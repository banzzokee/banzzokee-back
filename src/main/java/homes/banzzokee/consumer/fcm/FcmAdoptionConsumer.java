package homes.banzzokee.consumer.fcm;

import static homes.banzzokee.event.type.AdoptionAction.CREATE;
import static homes.banzzokee.event.type.AdoptionAction.STATUS;

import homes.banzzokee.consumer.error.exception.AdoptionNotFoundException;
import homes.banzzokee.consumer.error.exception.BusinessException;
import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.notification.dao.FcmSubscriptionRepository;
import homes.banzzokee.domain.notification.dao.NotificationRepository;
import homes.banzzokee.domain.notification.entity.Notification;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.event.AdoptionEvent;
import homes.banzzokee.event.dto.EntityStatusDto;
import homes.banzzokee.event.type.AdoptionAction;
import homes.banzzokee.infra.firebase.FcmService;
import homes.banzzokee.infra.firebase.dto.TopicMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmAdoptionConsumer {

  private final AdoptionRepository adoptionRepository;
  private final NotificationRepository notificationRepository;
  private final FcmSubscriptionRepository fcmSubscriptionRepository;
  private final FcmService fcmService;

  @Transactional
  @RabbitListener(queues = "queue.notify.fcm.adoption", errorHandler = "customErrorHandler")
  public void handleAdoptionEvent(AdoptionEvent event) {
    EntityStatusDto payload = event.getPayload();
    Long adoptionId = payload.getId();

    Adoption adoption = adoptionRepository.findById(adoptionId)
        .orElseThrow(() -> new AdoptionNotFoundException(adoptionId));

    String messageId = null;
    String notificationId = UUID.randomUUID().toString();
    TopicMessage message = createTopicMessage(notificationId, adoption,
        payload.getAction());

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
        Notification.of(notificationId,
            messageId,
            message.getTopic(),
            message.getTitle(),
            message.getBody(),
            message.getImage(),
            users));
  }

  private static TopicMessage createTopicMessage(String notificationId,
      Adoption adoption, AdoptionAction action) {
    Map<String, String> data = new HashMap<>();
    data.put("adoptionId", adoption.getId().toString());
    data.put("notificationId", notificationId);

    if (action == CREATE) {
      return createAdoptionRegisterMessage(adoption, data);
    } else if (action == STATUS) {
      return createAdoptionStatusChangeMessage(adoption, data);
    }

    throw new BusinessException("unsupported action: " + action);
  }

  private static TopicMessage createAdoptionRegisterMessage(Adoption adoption,
      Map<String, String> data) {
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

  private static TopicMessage createAdoptionStatusChangeMessage(Adoption adoption,
      Map<String, String> data) {
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
}
