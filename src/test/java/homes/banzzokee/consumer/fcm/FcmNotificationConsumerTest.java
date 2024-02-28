package homes.banzzokee.consumer.fcm;

import static homes.banzzokee.domain.type.AdoptionStatus.ADOPTING;
import static homes.banzzokee.domain.type.AdoptionStatus.FINISHED;
import static homes.banzzokee.domain.type.AdoptionStatus.RESERVING;
import static homes.banzzokee.event.type.EntityAction.ADOPTION_CREATED;
import static homes.banzzokee.event.type.EntityAction.ADOPTION_DELETED;
import static homes.banzzokee.event.type.EntityAction.ADOPTION_STATUS_CHANGED;
import static homes.banzzokee.event.type.EntityAction.ADOPTION_UPDATED;
import static homes.banzzokee.event.type.EntityAction.REVIEW_CREATED;
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
import static org.mockito.quality.Strictness.LENIENT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.messaging.FirebaseMessagingException;
import homes.banzzokee.consumer.error.exception.AdoptionNotFoundException;
import homes.banzzokee.consumer.error.exception.BusinessException;
import homes.banzzokee.consumer.error.exception.ReviewNotFoundException;
import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.notification.dao.FcmSubscriptionRepository;
import homes.banzzokee.domain.notification.dao.NotificationRepository;
import homes.banzzokee.domain.notification.entity.Notification;
import homes.banzzokee.domain.review.dao.ReviewRepository;
import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.event.EntityEvent;
import homes.banzzokee.infra.firebase.FcmService;
import homes.banzzokee.infra.firebase.dto.TopicMessage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings(strictness = LENIENT)
@ExtendWith(MockitoExtension.class)
class FcmNotificationConsumerTest {

  @InjectMocks
  private FcmNotificationConsumer consumer;

  @Mock
  private AdoptionRepository adoptionRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private FcmSubscriptionRepository fcmSubscriptionRepository;

  @Mock
  private FcmService fcmService;

  private static final String IMAGE = "./images.jpg";

  @BeforeEach
  public void setup() {
    User user1 = mock(User.class);
    User user2 = mock(User.class);

    given(fcmSubscriptionRepository.findUsersByTopic(anyString()))
        .willReturn(List.of(user1, user2));
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 게시글이 존재하지 않으면 AdoptionNotFoundException 발생")
  void handleEvent_when_adoptionNotExists_then_throwAdoptionNotFoundException() {
    // given
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThrows(AdoptionNotFoundException.class,
        () -> consumer.handleEvent(EntityEvent.of(1L, ADOPTION_CREATED), null, null));
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 게시글 등록 성공 검증")
  void handleEvent_when_success_then_verify()
      throws FirebaseMessagingException, JsonProcessingException {
    // given
    Adoption adoption = createMockAdoption(1L, ADOPTING);
    User user = mock(User.class);
    Shelter shelter = mock(Shelter.class);
    given(shelter.getId()).willReturn(10L);
    given(user.getShelter()).willReturn(shelter);
    given(adoption.getUser()).willReturn(user);
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    // when
    consumer.handleEvent(EntityEvent.of(1L, ADOPTION_CREATED), null, null);

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
    assertEquals(message.toJson(), notification.getMessage());
    assertThat(notification.getReceivers()).hasSize(2);
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 상태 예약중 성공 검증")
  void handleEvent_when_RESERVING_then_verify()
      throws FirebaseMessagingException, JsonProcessingException {
    // given
    Adoption adoption = createMockAdoption(1L, RESERVING);
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    // when
    consumer.handleEvent(EntityEvent.of(1L, ADOPTION_STATUS_CHANGED), null, null);

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
    assertEquals(message.toJson(), notification.getMessage());
    assertThat(notification.getReceivers()).hasSize(2);
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 상태 분양중 성공 검증")
  void handleEvent_when_ADOPTING_then_verify()
      throws FirebaseMessagingException, JsonProcessingException {
    // given
    Adoption adoption = createMockAdoption(1L, ADOPTING);
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    // when
    consumer.handleEvent(EntityEvent.of(1L, ADOPTION_STATUS_CHANGED), null, null);

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
    assertEquals(message.toJson(), notification.getMessage());
    assertThat(notification.getReceivers()).hasSize(2);
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 상태 분양완료 성공 검증")
  void handleEvent_when_FINISHED_then_verify()
      throws FirebaseMessagingException, JsonProcessingException {
    // given
    Adoption adoption = createMockAdoption(1L, FINISHED);
    given(adoptionRepository.findById(anyLong())).willReturn(Optional.of(adoption));

    // when
    consumer.handleEvent(EntityEvent.of(1L, ADOPTION_STATUS_CHANGED), null, null);

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
    assertEquals(message.toJson(), notification.getMessage());
    assertThat(notification.getReceivers()).hasSize(2);
  }

  @Test
  @DisplayName("[분양 게시글 이벤트 처리] - 지원하지 않는 액션 BusinessException 발생")
  void handleEvent_when_actionInvalid_then_throwBusinessException() {
    // when & then
    assertThrows(BusinessException.class,
        () -> consumer.handleEvent(EntityEvent.of(1L, ADOPTION_DELETED), null, null));

    assertThrows(BusinessException.class,
        () -> consumer.handleEvent(EntityEvent.of(1L, ADOPTION_UPDATED), null, null));
  }

  @Test
  @DisplayName("[리뷰 이벤트 처리] - 리뷰가 존재하지 않으면 ReviewNotFoundException 발생")
  void handleEvent_when_reviewNotExists_then_throwReviewNotFoundException() {
    // given
    given(reviewRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThrows(ReviewNotFoundException.class,
        () -> consumer.handleEvent(EntityEvent.of(1L, REVIEW_CREATED), null, null));
  }

  @Test
  @DisplayName("[리뷰 이벤트 처리] - 리뷰 등록 성공 검증")
  void handleEvent_when_reviewRegistered_then_verify()
      throws FirebaseMessagingException, JsonProcessingException {
    // given
    Adoption adoption = createMockAdoption(1L, FINISHED);
    Review review = createMockReview(1L, adoption);
    given(reviewRepository.findById(anyLong())).willReturn(Optional.of(review));

    // when
    consumer.handleEvent(EntityEvent.of(1L, REVIEW_CREATED), null, null);

    // then
    // 메시지 전송 검증
    ArgumentCaptor<TopicMessage> messageCaptor =
        ArgumentCaptor.forClass(TopicMessage.class);
    verify(fcmService).sendTopicMessage(messageCaptor.capture());
    TopicMessage message = messageCaptor.getValue();
    assertEquals("topic.adoption.1", message.getTopic());
    assertEquals("가족을 만난 반쪽이의 소식이 도착했어요!", message.getTitle());
    assertNull(message.getBody());
    assertNull(message.getImage());

    // 메시지 저장 검증
    ArgumentCaptor<Notification> notificationCaptor =
        ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(notificationCaptor.capture());
    Notification notification = notificationCaptor.getValue();
    assertEquals(message.toJson(), notification.getMessage());
    assertThat(notification.getReceivers()).hasSize(2);
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

  private static Review createMockReview(Long id, Adoption adoption) {
    Review mock = spy(Review.builder()
        .adoption(adoption)
        .content("강아지가 너무 귀여워요.")
        .build());
    given(mock.getId()).willReturn(id);
    return mock;
  }
}