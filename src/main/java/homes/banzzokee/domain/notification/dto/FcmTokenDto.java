package homes.banzzokee.domain.notification.dto;

import homes.banzzokee.domain.notification.entity.FcmToken;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmTokenDto {

  private final String token;

  private final Long userId;

  public static FcmTokenDto fromEntity(FcmToken fcmToken) {
    return FcmTokenDto.builder()
        .token(fcmToken.getToken())
        .userId(fcmToken.getUser().getId())
        .build();
  }
}
