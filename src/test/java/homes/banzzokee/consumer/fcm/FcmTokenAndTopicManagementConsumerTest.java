package homes.banzzokee.consumer.fcm;

import static homes.banzzokee.event.type.FcmTopicCategory.SHELTER;
import static homes.banzzokee.event.type.FcmTopicAction.SUBSCRIBE;
import static homes.banzzokee.event.type.FcmTopicAction.UNSUBSCRIBE;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import homes.banzzokee.consumer.error.exception.TokenNotFoundException;
import homes.banzzokee.domain.notification.dao.FcmSubscriptionRepository;
import homes.banzzokee.domain.notification.dao.FcmTokenRepository;
import homes.banzzokee.domain.notification.dto.FcmTokenDto;
import homes.banzzokee.domain.notification.entity.FcmSubscription;
import homes.banzzokee.domain.notification.entity.FcmToken;
import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.event.FcmTokenRegisteredEvent;
import homes.banzzokee.event.FcmTopicStatusChangeEvent;
import homes.banzzokee.event.dto.FcmTopicStatusDto;
import homes.banzzokee.infra.firebase.FcmService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FcmTokenAndTopicManagementConsumerTest {

  @InjectMocks
  private FcmTokenAndTopicManagementConsumer consumer;

  @Mock
  private FcmSubscriptionRepository fcmSubscriptionRepository;

  @Mock
  private FcmTokenRepository fcmTokenRepository;

  @Mock
  private ShelterRepository shelterRepository;

  @Mock
  private FcmService fcmService;

  @Test
  @DisplayName("[토큰 등록 처리] - 토큰이 존재하지 않으면 TokenNotFoundException 발생")
  void handleFcmTokenRegisteredEvent_when_tokenNotExists_then_throwTokenNotFoundException() {
    // given
    given(fcmTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

    // when & then
    FcmTokenRegisteredEvent event = FcmTokenRegisteredEvent.builder()
        .payload(FcmTokenDto.builder()
            .token("token")
            .userId(1L)
            .build())
        .build();

    assertThrows(TokenNotFoundException.class,
        () -> consumer.handleFcmTokenRegisteredEvent(event, null, null));
  }

  @Test
  @DisplayName("[토큰 등록 처리] - 성공 검증")
  void handleFcmTokenRegisteredEvent_when_success_then_verify()
      throws FirebaseMessagingException {
    // given
    User user = mock(User.class);
    given(user.getId()).willReturn(1L);
    FcmToken fcmToken = FcmToken.builder()
        .token("token")
        .user(user)
        .build();
    given(fcmTokenRepository.findByToken(anyString()))
        .willReturn(Optional.of(fcmToken));

    Shelter shelter1 = mock(Shelter.class);
    given(shelter1.getId()).willReturn(1L);
    Shelter shelter2 = mock(Shelter.class);
    given(shelter2.getId()).willReturn(2L);
    given(shelterRepository.findAllByFollower(anyLong()))
        .willReturn(Arrays.asList(shelter1, shelter2));

    TopicManagementResponse response = mock(TopicManagementResponse.class);
    given(response.getSuccessCount()).willReturn(1);
    given(fcmService.subscribeToTopic(anyList(), anyString())).willReturn(response);

    // when
    FcmTokenRegisteredEvent event = FcmTokenRegisteredEvent.builder()
        .payload(FcmTokenDto.fromEntity(fcmToken))
        .build();

    consumer.handleFcmTokenRegisteredEvent(event, null, null);

    // then
    verify(fcmService, times(2)).subscribeToTopic(anyList(), anyString());

    ArgumentCaptor<List<FcmSubscription>> captor = ArgumentCaptor.forClass(List.class);
    verify(fcmSubscriptionRepository).saveAll(captor.capture());
    assertThat(captor.getValue())
        .hasSize(2)
        .extracting(FcmSubscription::getFcmToken,
            FcmSubscription::getTopic,
            FcmSubscription::isValid)
        .containsExactlyInAnyOrder(
            tuple(fcmToken, "topic.shelter.1", true),
            tuple(fcmToken, "topic.shelter.2", true)
        );
  }

  @Test
  @DisplayName("[토픽 상태 변경 처리] - 액션이 SUBSCRIBE 시 subscribeTopicsAndSave 검증")
  void handleFcmTopicStatusChangeEvent_when_actionIsSubscribe_then_subscribeTopicsAndSave()
      throws FirebaseMessagingException {
    // given
    List<FcmToken> tokens = createFcmTokens(3);
    given(fcmTokenRepository.findAllByUserId(anyLong())).willReturn(tokens);

    TopicManagementResponse response = mock(TopicManagementResponse.class);
    given(fcmService.subscribeToTopic(anyList(), anyString())).willReturn(response);

    // when
    FcmTopicStatusChangeEvent event = FcmTopicStatusChangeEvent.builder()
        .payload(FcmTopicStatusDto.builder()
            .userId(1L)
            .topicCategory(SHELTER)
            .topicId(1L)
            .action(SUBSCRIBE)
            .build())
        .build();

    consumer.handleFcmTopicStatusChangeEvent(event, null, null);

    // then
    ArgumentCaptor<List<String>> tokensCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
    verify(fcmService).subscribeToTopic(tokensCaptor.capture(), topicCaptor.capture());
    assertThat(tokensCaptor.getValue())
        .hasSize(tokens.size())
        .containsExactlyInAnyOrder(
            tokens.stream().map(FcmToken::getToken).toArray(String[]::new));
    assertEquals("topic.shelter.1", topicCaptor.getValue());

    ArgumentCaptor<List<FcmSubscription>> subscriptionsCaptor = ArgumentCaptor.forClass(
        List.class);
    verify(fcmSubscriptionRepository).saveAll(subscriptionsCaptor.capture());
    assertThat(subscriptionsCaptor.getValue())
        .hasSize(tokens.size())
        .extracting(FcmSubscription::getFcmToken,
            FcmSubscription::getTopic,
            FcmSubscription::isValid)
        .containsExactlyInAnyOrder(
            tuple(tokens.get(0), "topic.shelter.1", true),
            tuple(tokens.get(1), "topic.shelter.1", true),
            tuple(tokens.get(2), "topic.shelter.1", true));
  }

  @Test
  @DisplayName("[토픽 상태 변경 처리] - 액션이 UNSUBSCRIBE 시 unsubscribeTopicsAndDelete 검증")
  void handleFcmTopicStatusChangeEvent_when_actionIsUnsubscribe_then_unsubscribeTopicsAndDelete()
      throws FirebaseMessagingException {
    // given
    List<FcmToken> tokens = createFcmTokens(3);
    given(fcmTokenRepository.findAllByUserId(anyLong())).willReturn(tokens);

    TopicManagementResponse response = mock(TopicManagementResponse.class);
    given(fcmService.unsubscribeFromTopic(anyList(), anyString())).willReturn(response);

    // when
    FcmTopicStatusChangeEvent event = FcmTopicStatusChangeEvent.builder()
        .payload(FcmTopicStatusDto.builder()
            .userId(1L)
            .topicCategory(SHELTER)
            .topicId(1L)
            .action(UNSUBSCRIBE)
            .build())
        .build();

    consumer.handleFcmTopicStatusChangeEvent(event, null, null);

    // then
    ArgumentCaptor<List<String>> tokensCaptor = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
    verify(fcmService).unsubscribeFromTopic(tokensCaptor.capture(),
        topicCaptor.capture());
    assertThat(tokensCaptor.getValue())
        .hasSize(tokens.size())
        .containsExactlyInAnyOrder(
            tokens.stream().map(FcmToken::getToken).toArray(String[]::new));
    assertEquals("topic.shelter.1", topicCaptor.getValue());

    ArgumentCaptor<List<FcmToken>> deleteTokensCaptor = ArgumentCaptor.forClass(
        List.class);
    verify(fcmSubscriptionRepository).deleteByFcmTokenIn(deleteTokensCaptor.capture());
    deleteTokensCaptor.getValue().stream().map(FcmToken::getToken)
        .forEach(token -> assertThat(tokensCaptor.getValue()).contains(token));
  }

  private static List<FcmToken> createFcmTokens(int count) {
    List<FcmToken> tokens = new ArrayList<>();

    IntStream.range(0, count).forEach(i -> {
      tokens.add(FcmToken.builder().token("token" + i).build());
    });

    return tokens.stream().toList();
  }
}