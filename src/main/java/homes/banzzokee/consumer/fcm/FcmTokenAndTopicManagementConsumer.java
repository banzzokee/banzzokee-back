package homes.banzzokee.consumer.fcm;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import com.google.firebase.messaging.TopicManagementResponse.Error;
import homes.banzzokee.consumer.error.exception.TokenNotFoundException;
import homes.banzzokee.domain.notification.dao.FcmSubscriptionRepository;
import homes.banzzokee.domain.notification.dao.FcmTokenRepository;
import homes.banzzokee.domain.notification.dto.FcmTokenDto;
import homes.banzzokee.domain.notification.entity.FcmSubscription;
import homes.banzzokee.domain.notification.entity.FcmToken;
import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.event.FcmTokenRegisteredEvent;
import homes.banzzokee.event.FcmTopicStatusChangeEvent;
import homes.banzzokee.event.dto.FcmTopicStatusDto;
import homes.banzzokee.infra.firebase.FcmService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenAndTopicManagementConsumer {

  private final FcmSubscriptionRepository fcmSubscriptionRepository;
  private final FcmTokenRepository fcmTokenRepository;
  private final ShelterRepository shelterRepository;
  private final FcmService fcmService;

  @Transactional
  @RabbitListener(queues = "queue.manage.fcm.token", errorHandler = "customErrorHandler")
  public void handleFcmTokenRegisteredEvent(FcmTokenRegisteredEvent event) {
    FcmTokenDto payload = event.getPayload();

    String token = payload.getToken();
    FcmToken fcmToken = getFcmTokenOrThrow(token);

    Long userId = payload.getUserId();
    List<String> topics = new ArrayList<>();
    topics.addAll(getSubscribableTopicsForFollowedShelters(userId));
    topics.addAll(getSubscribableTopicsForBookmarks(userId));

    subscribeTopicsAndSave(fcmToken, topics);
  }

  @Transactional
  @RabbitListener(queues = "queue.manage.fcm.topic", errorHandler = "customErrorHandler")
  public void handleFcmTopicStatusChangeEvent(FcmTopicStatusChangeEvent event) {
    FcmTopicStatusDto payload = event.getPayload();

    Long userId = payload.getUserId();
    List<FcmToken> tokens = fcmTokenRepository.findAllByUserId(userId);

    String topic = payload.getTopic();
    switch (payload.getAction()) {
      case SUBSCRIBE:
        subscribeTopicsAndSave(tokens, topic);
        break;
      case UNSUBSCRIBE:
        unsubscribeTopicsAndDelete(tokens, topic);
        break;
    }
  }

  /**
   * 토큰을 가져오거나 토큰이 없으면 예외를 발생시킵니다.
   *
   * @param token 토큰
   * @return 토큰
   */
  private FcmToken getFcmTokenOrThrow(String token) {
    return fcmTokenRepository.findByToken(token)
        .orElseThrow(() -> new TokenNotFoundException(token));
  }

  /**
   * 팔로우 중인 보호소의 목록을 가져와 구독할 토픽이름으로 반환합니다.
   *
   * @param userId 사용자 아이디
   * @return 구독할 토픽 이름 목록
   */
  private List<String> getSubscribableTopicsForFollowedShelters(Long userId) {
    return shelterRepository
        .findAllByFollower(userId)
        .stream()
        .map(shelter -> "topic.shelter." + shelter.getId())
        .toList();
  }

  /**
   * 북마크한 분양글을 가져와 구독할 토픽이름으로 반환합니다.
   *
   * @param userId 사용자 아이디
   * @return 구독할 토픽 이름 목록
   */
  private List<String> getSubscribableTopicsForBookmarks(Long userId) {
    return Collections.emptyList();
  }

  /**
   * 토큰 목록을 토픽에 구독하고 구독 정보를 저장합니다.
   *
   * @param fcmTokens 토큰 목록
   * @param topic     구독할 토픽
   */
  private void subscribeTopicsAndSave(List<FcmToken> fcmTokens, String topic) {
    subscribeTopicsAndSave(fcmTokens, Collections.singletonList(topic));
  }

  /**
   * 토픽을 구독하고 구독 정보를 저장합니다.
   *
   * @param fcmToken 토큰
   * @param topics   구독할 토픽 목록
   */
  private void subscribeTopicsAndSave(FcmToken fcmToken, List<String> topics) {
    subscribeTopicsAndSave(Collections.singletonList(fcmToken), topics);
  }

  /**
   * 토큰 목록을 토픽에 구독하고 구독 정보를 저장합니다.
   *
   * @param fcmTokens 토큰 목록
   * @param topics    구독할 토픽 목록
   */
  private void subscribeTopicsAndSave(List<FcmToken> fcmTokens, List<String> topics) {
    if (fcmTokens == null || fcmTokens.isEmpty() || topics == null || topics.isEmpty()) {
      return;
    }

    List<String> tokens = getTokensFromFcmTokens(fcmTokens);
    List<FcmSubscription> subscriptions = new ArrayList<>();

    topics.forEach(topic -> {
      TopicManagementResponse response = null;
      try {
        response = fcmService.subscribeToTopic(tokens, topic);
        Map<Integer, String> errors = getErrors(response);
        for (int i = 0; i < fcmTokens.size(); i++) {
          FcmToken token = fcmTokens.get(i);
          boolean isValid = !errors.containsKey(i);
          subscriptions.add(createSubscription(token, topic, isValid));
        }
      } catch (FirebaseMessagingException e) {
        log.error("subscribe token failed topic={}", topic, e);
      } finally {
        logResponse(response);
      }
    });

    fcmSubscriptionRepository.saveAll(subscriptions);
  }

  /**
   * 토큰 목록을 토픽에서 구독 해지하고 구독 정보를 삭제합니다.
   *
   * @param fcmTokens 토큰 목록
   * @param topic     구독 해지할 토픽
   */
  private void unsubscribeTopicsAndDelete(List<FcmToken> fcmTokens, String topic) {
    if (fcmTokens == null || fcmTokens.isEmpty() || topic == null) {
      return;
    }

    List<FcmToken> toDeleteTokens = new ArrayList<>();
    List<String> tokens = getTokensFromFcmTokens(fcmTokens);
    TopicManagementResponse response = null;
    try {
      response = fcmService.unsubscribeFromTopic(tokens, topic);
      Map<Integer, String> errors = getErrors(response);
      for (int i = 0; i < fcmTokens.size(); i++) {
        FcmToken token = fcmTokens.get(i);
        boolean isValid = errors.containsKey(i);
        if (!isValid) {
          toDeleteTokens.add(token);
        }
      }
    } catch (FirebaseMessagingException e) {
      log.error("unsubscribe token failed topic={}", topic, e);
    } finally {
      logResponse(response);
    }

    fcmSubscriptionRepository.deleteByFcmTokenIn(toDeleteTokens);
  }

  private void logResponse(TopicManagementResponse response) {
    if (response == null) {
      return;
    }
    log.trace("Success subscribes: {}", response.getSuccessCount());
    log.trace("Failed subscribes: {}", response.getFailureCount());
    for (TopicManagementResponse.Error error : response.getErrors()) {
      log.trace("\tIndex: {}, Reason: {}", error.getIndex(), error.getReason());
    }
  }

  private List<String> getTokensFromFcmTokens(List<FcmToken> tokens) {
    if (tokens == null || tokens.isEmpty()) {
      return Collections.emptyList();
    }
    return tokens.stream().map(FcmToken::getToken).toList();
  }

  private Map<Integer, String> getErrors(TopicManagementResponse response) {
    return response.getErrors()
        .stream()
        .collect(Collectors.toMap(Error::getIndex, Error::getReason));
  }

  private static FcmSubscription createSubscription(FcmToken token, String topic,
      boolean isValid) {
    return FcmSubscription.builder()
        .fcmToken(token)
        .topic(topic)
        .isValid(isValid)
        .build();
  }
}
