package homes.banzzokee.infra.firebase.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class MultiMessage extends Message<MulticastMessage> {

  private final List<String> tokens;

  static {
    objectMapper.addMixIn(MulticastMessage.class, MulticastMessageMixIn.class);
  }

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

  @Override
  public MulticastMessage toFcmMessage() {
    if (fcmMessage == null) {
      fcmMessage = MulticastMessage.builder()
          .setNotification(Notification.builder()
              .setTitle(getTitle())
              .setBody(getBody())
              .setImage(getImage())
              .build())
          .putAllData(getData())
          .addAllTokens(getTokens())
          .build();
    }
    return fcmMessage;
  }

  /**
   * MulticastMessage에서 tokens 필드는 JSON으로 변환하지 않는다.
   * <p>
   * MulticastMessage는 소스 수정이 불가능하기 때문에 mix-in을 사용하여 serialize/deserialize를 제어한다.
   * </p>
   */
  static abstract class MulticastMessageMixIn {

    @JsonIgnore
    private Collection<String> tokens;
  }
}
