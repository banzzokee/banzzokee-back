package homes.banzzokee.domain.user.dto;

import homes.banzzokee.global.validator.annotation.Password;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 탈퇴 요청
 */
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class WithdrawUserRequest {

  @Password
  private String password;

}
