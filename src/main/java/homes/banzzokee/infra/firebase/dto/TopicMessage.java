package homes.banzzokee.infra.firebase.dto;

import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class TopicMessage extends Message<com.google.firebase.messaging.Message> {

  private final String topic;

  public static TopicMessage of(String topic, String title, String body,
      String image,
      Map<String, String> data) {
    return TopicMessage.builder()
        .topic(topic)
        .title(title)
        .body(body)
        .image(image)
        .data(data)
        .build();
  }

  @Override
  public com.google.firebase.messaging.Message toFcmMessage() {
    if (fcmMessage == null) {
      fcmMessage = com.google.firebase.messaging.Message.builder()
          .setTopic(topic)
          .putAllData(getData())
          .setNotification(
              com.google.firebase.messaging.Notification.builder()
                  .setTitle(getTitle())
                  .setBody(getBody())
                  .setImage(getImage())
                  .build())
          .build();
    }
    return fcmMessage;
  }
}
