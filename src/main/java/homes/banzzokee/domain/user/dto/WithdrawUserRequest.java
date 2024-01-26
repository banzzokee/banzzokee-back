package homes.banzzokee.domain.user.dto;

import homes.banzzokee.global.validator.annotation.Password;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 회원 탈퇴 요청
 */
@Getter
@AllArgsConstructor
@Builder
public class WithdrawUserRequest {

  @Password
  private final String password;

}
