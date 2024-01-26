package homes.banzzokee.domain.user.dto;

import homes.banzzokee.global.validator.annotation.Password;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 패스워드 변경 요청
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {

  @Password
  private String originPassword;

  @Password
  private String newPassword;

  @Password
  private String confirmPassword;

}
