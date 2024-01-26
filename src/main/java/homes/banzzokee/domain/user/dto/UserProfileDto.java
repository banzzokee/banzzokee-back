package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.shelter.dto.ShelterDto;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.entity.User;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 프로필
 */
@Builder
@AllArgsConstructor
@Getter
public class UserProfileDto {

  private final Long userId;

  private final String email;

  private final String profileImgUrl;

  private final String nickname;

  private final String introduce;

  private final LocalDate joinedAt;

  private final ShelterDto shelter;

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
