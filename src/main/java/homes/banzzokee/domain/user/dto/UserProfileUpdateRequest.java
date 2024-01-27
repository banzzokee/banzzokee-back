package homes.banzzokee.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 사용자 프로필 수정 요청
 */
@Getter
@Builder
@EqualsAndHashCode
public class UserProfileUpdateRequest {

  /**
   * 닉네임
   */
  @NotBlank
  private final String nickname;

  /**
   * 자기소개
   */
  @NotBlank
  private final String introduce;
}
