package homes.banzzokee.domain.auth.dto;

import homes.banzzokee.global.validator.annotation.EmailValid;
import homes.banzzokee.global.validator.annotation.Password;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignupRequest {

  /**
   * 회원가입 Request Dto
   * email
   * password
   * confirmPassword
   * nickname
   */

  @EmailValid
  private final String email;

  @Password
  private final String password;

  @Password
  private final String confirmPassword;

  @Size(max = 10, message = "nickname 은 10자리 이내로 작성해주세요.")
  @NotBlank(message = "nickname 은 필수 입력 항목입니다.")
  private final String nickname;

}