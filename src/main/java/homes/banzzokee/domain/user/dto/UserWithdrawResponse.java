package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

/**
 * 회원 탈퇴 응답
 */
@Getter
@Builder
public class UserWithdrawResponse {

  private final Long userId;

  private final String email;

  public static UserWithdrawResponse fromEntity(User user) {
    return UserWithdrawResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .build();
  }
}
