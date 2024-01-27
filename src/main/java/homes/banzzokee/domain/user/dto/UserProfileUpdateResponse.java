package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.user.entity.User;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 사용자 프로필 수정 응답
 */
@Getter
@Builder
@EqualsAndHashCode
public class UserProfileUpdateResponse {

  /**
   * 사용자 아이디
   */
  private final Long userId;

  /**
   * 이메일
   */
  private final String email;

  /**
   * 프로필 이미지 경로
   */
  private final String profileImgUrl;

  /**
   * 닉네임
   */
  private final String nickname;

  /**
   * 자기소개
   */
  private final String introduce;

  /**
   * 수정일시
   */
  private final LocalDateTime updatedAt;

  public static UserProfileUpdateResponse fromEntity(User user) {
    return UserProfileUpdateResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .profileImgUrl(user.getProfileImageUrl())
        .nickname(user.getNickname())
        .introduce(user.getIntroduce())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
