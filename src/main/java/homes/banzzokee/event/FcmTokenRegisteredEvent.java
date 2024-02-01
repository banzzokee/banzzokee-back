package homes.banzzokee.event;

import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.domain.notification.dto.FcmTokenDto;
import homes.banzzokee.infra.rabbitmq.event.BaseMessage;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * FCM 토큰 등록 이벤트
 */
@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
public class FcmTokenRegisteredEvent extends BaseMessage<FcmTokenDto> {

  @Override
  public String getRoutingKey() {
    return "token.registered";
  }

  public static FcmTokenRegisteredEvent from(FcmTokenDto fcmToken) {
    return FcmTokenRegisteredEvent.builder()
        .payload(fcmToken)
        .build();
  }
}
