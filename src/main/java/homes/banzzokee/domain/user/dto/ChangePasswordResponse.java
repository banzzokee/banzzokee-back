package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.user.entity.User;
import lombok.Builder;

/**
 * 패스워드 변경 응답
 *
 * @param userId 사용자 아이디
 * @param email  이메일
 */
@Builder
public record ChangePasswordResponse(
    Long userId,
    String email
) {

  public static ChangePasswordResponse fromEntity(User user) {
    return ChangePasswordResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .build();
  }
}
