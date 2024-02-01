package homes.banzzokee.consumer.fcm;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
import homes.banzzokee.domain.notification.event.FcmTokenRegisteredEvent;
import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.infra.firebase.FcmService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FcmTokenRegisteredEventListenerTest {

  @InjectMocks
  private FcmTokenRegisteredEventListener eventListener;

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
  void handle_when_tokenNotExists_then_throwTokenNotFoundException() {
    // given
    given(fcmTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

    // when & then
    FcmTokenRegisteredEvent event = FcmTokenRegisteredEvent.builder()
        .payload(FcmTokenDto.builder()
            .token("token")
            .userId(1L)
            .build())
        .build();

    assertThrows(TokenNotFoundException.class, () -> eventListener.handle(event));
  }

  @Test
  @DisplayName("[토큰 등록 처리] - 성공 검증")
  void handle_when_success_then_verify() throws FirebaseMessagingException {
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

    eventListener.handle(event);

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
            tuple(fcmToken, "topic.shelters.1", true),
            tuple(fcmToken, "topic.shelters.2", true)
        );
  }
}