package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.shelter.dto.ShelterDto;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.entity.User;
import java.time.LocalDate;
import lombok.Builder;

/**
 * 사용자 프로필
 *
 * @param userId        사용자 아이디
 * @param email         이메일
 * @param profileImgUrl 프로필 이미지 경로
 * @param nickname      닉네임
 * @param introduce     자기소개
 * @param joinedAt      가입일
 * @param shelter       보호소
 */
@Builder
public record UserProfileDto(
    Long userId,
    String email,
    String profileImgUrl,
    String nickname,
    String introduce,
    LocalDate joinedAt,
    ShelterDto shelter
) {

  public static UserProfileDto fromEntity(User user) {
    return UserProfileDto.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .profileImgUrl(user.getProfileImgUrl())
        .nickname(user.getNickname())
        .introduce(user.getIntroduce())
        .joinedAt(user.getCreatedAt().toLocalDate())
        .shelter(getShelterDto(user.getShelter()))
        .build();
  }

  private static ShelterDto getShelterDto(Shelter shelter) {
    if (shelter != null && shelter.isVerified()) {
      return ShelterDto.fromEntity(shelter);
    }
    return null;
  }
}
