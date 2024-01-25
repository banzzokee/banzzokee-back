package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.user.entity.User;
import lombok.Builder;

/**
 * 회원 탈퇴 응답
 *
 * @param userId 사용자 아이디
 * @param email  이메일
 */
@Builder
public record WithdrawUserResponse(
    Long userId,
    String email
) {

  public static WithdrawUserResponse fromEntity(User user) {
    return WithdrawUserResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .build();
  }
}
