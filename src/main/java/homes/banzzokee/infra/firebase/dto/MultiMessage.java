package homes.banzzokee.infra.firebase.dto;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class MultiMessage extends Message {

  private final List<String> tokens;

  public static MultiMessage of(List<String> tokens, String title, String body,
      String image,
      Map<String, String> data) {
    return MultiMessage.builder()
        .tokens(tokens)
        .title(title)
        .body(body)
        .image(image)
        .data(data)
        .build();
  }
}
