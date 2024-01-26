package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 회원 탈퇴 응답
 */
@Builder
@Getter
@AllArgsConstructor
public class WithdrawUserResponse {

  private final Long userId;

  private final String email;

  public static WithdrawUserResponse fromEntity(User user) {
    return WithdrawUserResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .build();
  }
}
