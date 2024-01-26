package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 패스워드 변경 응답
 */
@Builder
@AllArgsConstructor
@Getter
public class ChangePasswordResponse {

  private final Long userId;

  private final String email;

  public static ChangePasswordResponse fromEntity(User user) {
    return ChangePasswordResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .build();
  }
}
