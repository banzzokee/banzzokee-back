package homes.banzzokee.domain.user.dto;

import homes.banzzokee.global.validator.annotation.Password;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 패스워드 변경 요청
 */
@Builder
@Getter
@AllArgsConstructor
public class ChangePasswordRequest {

  @Password
  private final String originPassword;

  @Password
  private final String newPassword;

  @Password
  private final String confirmPassword;

}
