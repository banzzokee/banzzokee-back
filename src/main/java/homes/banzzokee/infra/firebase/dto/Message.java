package homes.banzzokee.infra.firebase.dto;

import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class Message {

  private final String title;
  private final String body;
  private final String image;
  private final Map<String, String> data;
}