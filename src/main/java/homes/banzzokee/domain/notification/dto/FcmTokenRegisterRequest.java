package homes.banzzokee.domain.notification.dto;

import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * FCM 토큰 등록 요청
 */
@Getter
@Builder
@RequiredArgsConstructor(onConstructor_ = @JsonCreator, access = PRIVATE)
@EqualsAndHashCode
public class FcmTokenRegisterRequest {

  @NotBlank
  private final String token;
}
