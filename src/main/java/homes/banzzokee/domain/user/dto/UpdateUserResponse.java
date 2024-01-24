package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.user.entity.User;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * 사용자 프로필 수정 응답
 *
 * @param userId        사용자 아이디
 * @param email         이메일
 * @param profileImgUrl 프로필 이미지 경로
 * @param nickname      닉네임
 * @param introduce     자기소개
 * @param updatedAt     수정일시
 */
@Builder
public record UpdateUserResponse(
    Long userId,
    String email,
    String profileImgUrl,
    String nickname,
    String introduce,
    LocalDateTime updatedAt
) {

  public static UpdateUserResponse fromEntity(User user) {
    return UpdateUserResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .profileImgUrl(user.getProfileImageUrl())
        .nickname(user.getNickname())
        .introduce(user.getIntroduce())
        .updatedAt(user.getUpdatedAt())
        .build();
  }
}
