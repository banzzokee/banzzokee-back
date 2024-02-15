package homes.banzzokee.consumer.fcm;

import com.google.firebase.messaging.FirebaseMessagingException;
import homes.banzzokee.consumer.error.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.notification.dao.FcmSubscriptionRepository;
import homes.banzzokee.domain.notification.dao.NotificationRepository;
import homes.banzzokee.domain.notification.entity.Notification;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.event.AdoptionEvent;
import homes.banzzokee.event.dto.EntityStatusDto;
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
    TopicMessage message = createTopicMessage(notificationId, adoption);

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

  private static String getTopic(Adoption adoption) {
    return "topic.shelter." + adoption.getUser().getShelter().getId();
  }

  private static TopicMessage createTopicMessage(String notificationId,
      Adoption adoption) {
    Optional<S3Object> image = adoption.getImages().stream().findFirst();
    Map<String, String> data = new HashMap<>();
    data.put("adoptionId", adoption.getId().toString());
    data.put("notificationId", notificationId);

    return TopicMessage.builder()
        .topic(getTopic(adoption))
        .title(adoption.getTitle())
        .body(adoption.getContent())
        .image(image.isPresent() ? image.map(S3Object::getUrl).orElse(null) : null)
        .data(data)
        .build();
  }
}
