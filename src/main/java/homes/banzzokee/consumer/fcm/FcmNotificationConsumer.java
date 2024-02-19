package homes.banzzokee.consumer.fcm;

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
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.event.EntityEvent;
import homes.banzzokee.event.dto.EntityStatusDto;
import homes.banzzokee.infra.firebase.FcmService;
import homes.banzzokee.infra.firebase.dto.TopicMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmNotificationConsumer {

  private final AdoptionRepository adoptionRepository;
  private final ReviewRepository reviewRepository;
  private final NotificationRepository notificationRepository;
  private final FcmSubscriptionRepository fcmSubscriptionRepository;
  private final FcmService fcmService;

  @Transactional
  @RabbitListener(queues = {"queue.notify.fcm.adoption",
      "queue.notify.fcm.review"}, errorHandler = "customErrorHandler")
  public void handleEvent(EntityEvent event) {
    String messageId = null;
    TopicMessage message = createTopicMessage(event.getPayload());

    try {
      messageId = fcmService.sendTopicMessage(message);
      log.info("send message success, topic={}, messageId={}",
          message.getTopic(),
          messageId);
    } catch (Exception e) {
      log.error("send message failed, topic={}", message.getTopic(), e);
    }

    List<User> users = fcmSubscriptionRepository.findUsersByTopic(message.getTopic());
    notificationRepository.save(
        Notification.of(message.getId(),
            messageId,
            message.getTopic(),
            message.getTitle(),
            message.getBody(),
            message.getImage(),
            users));
  }

  private TopicMessage createTopicMessage(EntityStatusDto payload) {
    Map<String, String> data = new HashMap<>();

    return switch (payload.getAction()) {
      case ADOPTION_CREATED -> createAdoptionRegisterMessage(payload.getId(), data);
      case ADOPTION_STATUS_CHANGED ->
          createAdoptionStatusChangeMessage(payload.getId(), data);
      case REVIEW_CREATED -> createReviewRegisterMessage(payload.getId(), data);
      default ->
          throw new BusinessException("unsupported action: " + payload.getAction());
    };
  }

  private TopicMessage createAdoptionRegisterMessage(Long adoptionId,
      Map<String, String> data) {
    Adoption adoption = findAdoptionOrThrow(adoptionId);
    data.put("adoptionId", adoption.getId().toString());
    String topic = "topic.shelter." + adoption.getUser().getShelter().getId();
    Optional<S3Object> image = adoption.getImages().stream().findFirst();
    return TopicMessage.builder()
        .topic(topic)
        .title("새로운 반쪽이가 가족을 찾고있어요!")
        .body(adoption.getTitle())
        .image(image.isPresent() ? image.map(S3Object::getUrl).orElse(null) : null)
        .data(data)
        .build();
  }

  private TopicMessage createAdoptionStatusChangeMessage(Long adoptionId,
      Map<String, String> data) {
    Adoption adoption = findAdoptionOrThrow(adoptionId);
    data.put("adoptionId", adoption.getId().toString());
    String topic = "topic.adoption." + adoption.getId();
    String title = switch (adoption.getStatus()) {
      case ADOPTING -> "반쪽이가 가족을 찾고있어요!";
      case RESERVING -> "반쪽이가 가족을 만날 예정이에요!";
      case FINISHED -> "반쪽이가 가족을 만났어요!";
    };
    return TopicMessage.builder()
        .topic(topic)
        .title(title)
        .data(data)
        .build();
  }

  private TopicMessage createReviewRegisterMessage(Long reviewId,
      Map<String, String> data) {
    Review review = findReviewOrThrow(reviewId);
    data.put("reviewId", review.getId().toString());
    String topic = "topic.adoption." + review.getAdoption().getId();
    return TopicMessage.builder()
        .topic(topic)
        .title("가족을 만난 반쪽이의 소식이 도착했어요!")
        .body(review.getTitle())
        .data(data)
        .build();
  }

  private Adoption findAdoptionOrThrow(Long adoptionId) {
    return adoptionRepository.findById(adoptionId)
        .orElseThrow(() -> new AdoptionNotFoundException(adoptionId));
  }

  private Review findReviewOrThrow(Long reviewId) {
    return reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ReviewNotFoundException(reviewId));
  }
}
