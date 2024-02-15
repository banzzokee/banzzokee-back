package homes.banzzokee.consumer.fcm;

import static homes.banzzokee.event.type.EntityAction.CREATE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.messaging.FirebaseMessagingException;
import homes.banzzokee.consumer.error.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.notification.dao.FcmSubscriptionRepository;
import homes.banzzokee.domain.notification.dao.NotificationRepository;
import homes.banzzokee.domain.notification.entity.FcmToken;
import homes.banzzokee.domain.notification.entity.Notification;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.event.AdoptionEvent;
import homes.banzzokee.infra.firebase.FcmService;
import homes.banzzokee.infra.firebase.dto.TopicMessage;
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
class FcmAdoptionConsumerTest {

  @InjectMocks
  private FcmAdoptionConsumer consumer;

  @Mock
  private AdoptionRepository adoptionRepository;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private FcmSubscriptionRepository fcmSubscriptionRepository;

  @Mock
  private FcmService fcmService;

  @Test
  @DisplayName("[분양 게시글 등록 처리] - 게시글이 존재하지 않으면 AdoptionNotFoundException 발생")
  void handleAdoptionEvent_when_adoptionNotExists_then_throwAdoptionNotFoundException() {
    // given
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThrows(AdoptionNotFoundException.class,
        () -> consumer.handleAdoptionEvent(AdoptionEvent.of(1L, CREATE)));
  }

  @Test
  @DisplayName("[분양 게시글 등록 처리] - 성공 검증")
  void handleAdoptionEvent_when_success_then_verify()
      throws FirebaseMessagingException, JsonProcessingException {
    // given
    String imageUrl = "./images.jpg";
    Adoption adoption = spy(Adoption.builder()
        .title("귀여운 강아지 분양합니다.")
        .content("귀여운 강아지에게 가족이 되어주세요.")
        .images(List.of(new S3Object(imageUrl)))
        .build());
    given(adoption.getId()).willReturn(1L);
    User user = mock(User.class);
    Shelter shelter = mock(Shelter.class);
    given(shelter.getId()).willReturn(10L);
    given(user.getShelter()).willReturn(shelter);
    given(adoption.getUser()).willReturn(user);
    given(adoption.getContent()).willReturn("귀여운 강아지에게 가족이 되어주세요.");
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    User user1 = mock(User.class);
    User user2 = mock(User.class);
    given(fcmSubscriptionRepository.findUsersByTopic(anyString()))
        .willReturn(List.of(user1, user2));

    // when
    consumer.handleAdoptionEvent(AdoptionEvent.of(1L, CREATE));

    // then
    // 메시지 전송 검증
    ArgumentCaptor<TopicMessage> messageCaptor =
        ArgumentCaptor.forClass(TopicMessage.class);
    verify(fcmService).sendTopicMessage(messageCaptor.capture());
    TopicMessage message = messageCaptor.getValue();
    assertEquals(message.getTopic(), "topic.shelter.10");
    assertEquals(message.getTitle(), adoption.getTitle());
    assertEquals(message.getBody(), adoption.getContent());
    assertEquals(message.getImage(), imageUrl);

    // 메시지 저장 검증
    ArgumentCaptor<Notification> notificationCaptor =
        ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(notificationCaptor.capture());
    Notification notification = notificationCaptor.getValue();
    assertEquals(notification.getTopic(), "topic.shelter.10");
    assertEquals(notification.getTitle(), adoption.getTitle());
    assertEquals(notification.getBody(), adoption.getContent());
    assertEquals(notification.getImage(), imageUrl);
    assertThat(notification.getReceivers()).hasSize(2);
  }
}