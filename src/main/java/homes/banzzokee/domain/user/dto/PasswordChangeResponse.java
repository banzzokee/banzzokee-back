package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 패스워드 변경 응답
 */
@Getter
@Builder
public class PasswordChangeResponse {

  private final Long userId;

  private final String email;

  public static PasswordChangeResponse fromEntity(User user) {
    return PasswordChangeResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .build();
  }
}
