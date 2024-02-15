package homes.banzzokee.consumer.fcm;

import static homes.banzzokee.domain.type.AdoptionStatus.ADOPTING;
import static homes.banzzokee.domain.type.AdoptionStatus.FINISHED;
import static homes.banzzokee.domain.type.AdoptionStatus.RESERVING;
import static homes.banzzokee.event.type.AdoptionAction.CREATE;
import static homes.banzzokee.event.type.AdoptionAction.DELETE;
import static homes.banzzokee.event.type.AdoptionAction.STATUS;
import static homes.banzzokee.event.type.AdoptionAction.UPDATE;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import homes.banzzokee.consumer.error.exception.BusinessException;
import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.notification.dao.FcmSubscriptionRepository;
import homes.banzzokee.domain.notification.dao.NotificationRepository;
import homes.banzzokee.domain.notification.entity.Notification;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.type.AdoptionStatus;
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

  private static final String IMAGE = "./images.jpg";

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 게시글이 존재하지 않으면 AdoptionNotFoundException 발생")
  void handleAdoptionEvent_when_adoptionNotExists_then_throwAdoptionNotFoundException() {
    // given
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThrows(AdoptionNotFoundException.class,
        () -> consumer.handleAdoptionEvent(AdoptionEvent.of(1L, CREATE)));
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 게시글 등록 성공 검증")
  void handleAdoptionEvent_when_success_then_verify()
      throws FirebaseMessagingException, JsonProcessingException {
    // given
    Adoption adoption = createMockAdoption(1L, ADOPTING);
    User user = mock(User.class);
    Shelter shelter = mock(Shelter.class);
    given(shelter.getId()).willReturn(10L);
    given(user.getShelter()).willReturn(shelter);
    given(adoption.getUser()).willReturn(user);
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
    assertEquals("topic.shelter.10", message.getTopic());
    assertEquals("새로운 반쪽이가 가족을 찾고있어요!", message.getTitle());
    assertEquals(adoption.getTitle(), message.getBody());
    assertEquals(IMAGE, message.getImage());

    // 메시지 저장 검증
    ArgumentCaptor<Notification> notificationCaptor =
        ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(notificationCaptor.capture());
    Notification notification = notificationCaptor.getValue();
    assertEquals("topic.shelter.10", notification.getTopic());
    assertEquals("새로운 반쪽이가 가족을 찾고있어요!", notification.getTitle());
    assertEquals(adoption.getTitle(), notification.getBody());
    assertEquals(IMAGE, notification.getImage());
    assertThat(notification.getReceivers()).hasSize(2);
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 상태 예약중 성공 검증")
  void handleAdoptionEvent_when_RESERVING_then_verify()
      throws FirebaseMessagingException, JsonProcessingException {
    // given
    Adoption adoption = createMockAdoption(1L, RESERVING);
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    User user1 = mock(User.class);
    User user2 = mock(User.class);
    given(fcmSubscriptionRepository.findUsersByTopic(anyString()))
        .willReturn(List.of(user1, user2));

    // when
    consumer.handleAdoptionEvent(AdoptionEvent.of(1L, STATUS));

    // then
    // 메시지 전송 검증
    ArgumentCaptor<TopicMessage> messageCaptor =
        ArgumentCaptor.forClass(TopicMessage.class);
    verify(fcmService).sendTopicMessage(messageCaptor.capture());
    TopicMessage message = messageCaptor.getValue();
    assertEquals("topic.adoption.1", message.getTopic());
    assertEquals("반쪽이가 가족을 만날 예정이에요!", message.getTitle());
    assertNull(message.getBody());
    assertNull(message.getImage());

    // 메시지 저장 검증
    ArgumentCaptor<Notification> notificationCaptor =
        ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(notificationCaptor.capture());
    Notification notification = notificationCaptor.getValue();
    assertEquals("topic.adoption.1", notification.getTopic());
    assertEquals("반쪽이가 가족을 만날 예정이에요!", notification.getTitle());
    assertNull(notification.getBody());
    assertNull(notification.getImage());
    assertThat(notification.getReceivers()).hasSize(2);
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 상태 분양중 성공 검증")
  void handleAdoptionEvent_when_ADOPTING_then_verify()
      throws FirebaseMessagingException, JsonProcessingException {
    // given
    Adoption adoption = createMockAdoption(1L, ADOPTING);
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    User user1 = mock(User.class);
    User user2 = mock(User.class);
    given(fcmSubscriptionRepository.findUsersByTopic(anyString()))
        .willReturn(List.of(user1, user2));

    // when
    consumer.handleAdoptionEvent(AdoptionEvent.of(1L, STATUS));

    // then
    // 메시지 전송 검증
    ArgumentCaptor<TopicMessage> messageCaptor =
        ArgumentCaptor.forClass(TopicMessage.class);
    verify(fcmService).sendTopicMessage(messageCaptor.capture());
    TopicMessage message = messageCaptor.getValue();
    assertEquals("topic.adoption.1", message.getTopic());
    assertEquals("반쪽이가 가족을 찾고있어요!", message.getTitle());
    assertNull(message.getBody());
    assertNull(message.getImage());

    // 메시지 저장 검증
    ArgumentCaptor<Notification> notificationCaptor =
        ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(notificationCaptor.capture());
    Notification notification = notificationCaptor.getValue();
    assertEquals("topic.adoption.1", notification.getTopic());
    assertEquals("반쪽이가 가족을 찾고있어요!", notification.getTitle());
    assertNull(notification.getBody());
    assertNull(notification.getImage());
    assertThat(notification.getReceivers()).hasSize(2);
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 상태 분양완료 성공 검증")
  void handleAdoptionEvent_when_FINISHED_then_verify()
      throws FirebaseMessagingException, JsonProcessingException {
    // given
    Adoption adoption = createMockAdoption(1L, FINISHED);
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    User user1 = mock(User.class);
    User user2 = mock(User.class);
    given(fcmSubscriptionRepository.findUsersByTopic(anyString()))
        .willReturn(List.of(user1, user2));

    // when
    consumer.handleAdoptionEvent(AdoptionEvent.of(1L, STATUS));

    // then
    // 메시지 전송 검증
    ArgumentCaptor<TopicMessage> messageCaptor =
        ArgumentCaptor.forClass(TopicMessage.class);
    verify(fcmService).sendTopicMessage(messageCaptor.capture());
    TopicMessage message = messageCaptor.getValue();
    assertEquals("topic.adoption.1", message.getTopic());
    assertEquals("반쪽이가 가족을 만났어요!", message.getTitle());
    assertNull(message.getBody());
    assertNull(message.getImage());

    // 메시지 저장 검증
    ArgumentCaptor<Notification> notificationCaptor =
        ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(notificationCaptor.capture());
    Notification notification = notificationCaptor.getValue();
    assertEquals("topic.adoption.1", notification.getTopic());
    assertEquals("반쪽이가 가족을 만났어요!", notification.getTitle());
    assertNull(notification.getBody());
    assertNull(notification.getImage());
    assertThat(notification.getReceivers()).hasSize(2);
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 지원하지 않는 액션 BusinessException 발생")
  void handleAdoptionEvent_when_actionInvalid_then_throwBusinessException() {
    // given
    Adoption adoption = mock(Adoption.class);
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    // when & then
    assertThrows(BusinessException.class,
        () -> consumer.handleAdoptionEvent(AdoptionEvent.of(1L, DELETE)));

    assertThrows(BusinessException.class,
        () -> consumer.handleAdoptionEvent(AdoptionEvent.of(1L, UPDATE)));
  }

  private static Adoption createMockAdoption(Long id, AdoptionStatus status) {
    Adoption mock = spy(Adoption.builder()
        .title("귀여운 강아지 분양합니다.")
        .content("귀여운 강아지에게 가족이 되어주세요.")
        .images(List.of(new S3Object(IMAGE)))
        .status(status)
        .build());
    given(mock.getId()).willReturn(id);
    return mock;
  }
}