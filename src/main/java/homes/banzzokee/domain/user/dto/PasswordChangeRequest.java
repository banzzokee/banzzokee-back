package homes.banzzokee.domain.user.dto;

import homes.banzzokee.global.validator.annotation.Password;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 패스워드 변경 요청
 */
@Getter
@Builder
@EqualsAndHashCode
public class PasswordChangeRequest {

  @Password
  private final String originPassword;

  @Password
  private final String newPassword;

  @Password
  private final String confirmPassword;

}
