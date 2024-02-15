package homes.banzzokee.infra.firebase.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopicMessage {

  private final String topic;
  private final String title;
  private final String body;
  private final String image;
  private final Map<String, String> data;
}
