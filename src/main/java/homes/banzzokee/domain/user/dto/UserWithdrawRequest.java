package homes.banzzokee.domain.user.dto;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

import com.fasterxml.jackson.annotation.JsonCreator;
import homes.banzzokee.global.validator.annotation.Password;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원 탈퇴 요청
 */
@Getter
@Builder
@RequiredArgsConstructor(onConstructor_ = @JsonCreator, access = PRIVATE)
public class UserWithdrawRequest {

  @Password
  private final String password;
}
