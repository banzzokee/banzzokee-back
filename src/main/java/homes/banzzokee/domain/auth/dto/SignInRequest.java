package homes.banzzokee.domain.auth.dto;

import homes.banzzokee.global.validator.annotation.EmailValid;
import homes.banzzokee.global.validator.annotation.Password;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignInRequest {

  @EmailValid
  private final String email;

  @Password
  private final String password;
}
