package homes.banzzokee.infra.firebase.dto;

import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class TopicMessage extends Message {

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
}
