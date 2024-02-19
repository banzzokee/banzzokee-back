package homes.banzzokee.infra.firebase.dto;

import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopicMessage {

  private final String id = UUID.randomUUID().toString();
  private final String topic;
  private final String title;
  private final String body;
  private final String image;
  private final Map<String, String> data;

  public TopicMessage(String topic, String title, String body, String image,
      Map<String, String> data) {
    this.topic = topic;
    this.title = title;
    this.body = body;
    this.image = image;
    this.data = data;

    if (data != null) {
      data.put("notificationId", id);
    }
  }
}
