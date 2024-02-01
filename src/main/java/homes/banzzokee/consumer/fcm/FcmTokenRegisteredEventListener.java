package homes.banzzokee.consumer.fcm;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import homes.banzzokee.consumer.error.exception.TokenNotFoundException;
import homes.banzzokee.domain.notification.dao.FcmSubscriptionRepository;
import homes.banzzokee.domain.notification.dao.FcmTokenRepository;
import homes.banzzokee.domain.notification.dto.FcmTokenDto;
import homes.banzzokee.domain.notification.entity.FcmSubscription;
import homes.banzzokee.domain.notification.entity.FcmToken;
import homes.banzzokee.domain.notification.event.FcmTokenRegisteredEvent;
import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.infra.firebase.FcmService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 하나의 토큰으로 같은 토큰을 여러번 구독해도 문제 없음
// 구독을 여러번 해지해도 문제 없음

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenRegisteredEventListener {

  private final FcmSubscriptionRepository fcmSubscriptionRepository;
  private final FcmTokenRepository fcmTokenRepository;
  private final ShelterRepository shelterRepository;
  private final FcmService fcmService;

  @Transactional
  @RabbitListener(queues = "queue.manage.fcm.token", errorHandler = "customErrorHandler")
  public void handle(FcmTokenRegisteredEvent event) {
    FcmTokenDto payload = event.getPayload();
    String token = payload.getToken();
    Long userId = payload.getUserId();

    FcmToken fcmToken = getFcmTokenOrThrow(token);

    List<String> topics = new ArrayList<>();
    topics.addAll(getSubscribableTopicsForFollowedShelters(userId));
    topics.addAll(getSubscribableTopicsForBookmarks(userId));

    subscribeTopicsAndSave(fcmToken, topics);
  }

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
        .map(shelter -> "topic.shelters." + shelter.getId())
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
   * 토픽을 구독하고 구독 정보를 저장합니다.
   *
   * @param token  토큰
   * @param topics 구독할 토픽 목록
   */
  private void subscribeTopicsAndSave(FcmToken token, List<String> topics) {
    if (topics == null || topics.isEmpty()) {
      return;
    }

    List<String> tokens = Collections.singletonList(token.getToken());
    List<FcmSubscription> subscriptions = new ArrayList<>();

    topics.forEach(topic -> {
      boolean subscribeSuccess = false;

      try {
        TopicManagementResponse response = fcmService.subscribeToTopic(tokens, topic);
        logResponse(response);
        subscribeSuccess = response.getSuccessCount() > 0;
      } catch (FirebaseMessagingException e) {
        log.error("subscribe token failed topic={}, token={}", topic, token, e);
      }

      subscriptions.add(FcmSubscription.builder()
          .fcmToken(token)
          .topic(topic)
          .isValid(subscribeSuccess)
          .build());
    });

    fcmSubscriptionRepository.saveAll(subscriptions);
  }

  private void logResponse(TopicManagementResponse response) {
    log.info("Success subscribes: {}", response.getSuccessCount());
    log.info("Failed subscribes: {}", response.getFailureCount());
    for (TopicManagementResponse.Error error : response.getErrors()) {
      log.info("\tIndex: {}, Reason: {}", error.getIndex(), error.getReason());
    }
  }
}
