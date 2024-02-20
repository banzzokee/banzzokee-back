package homes.banzzokee.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import homes.banzzokee.domain.shelter.dto.ShelterDto;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.entity.Follow;
import homes.banzzokee.domain.user.entity.User;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 사용자 프로필
 */
@Getter
@Builder
public class UserProfileDto {

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
   * 가입일
   */
  @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
  private final LocalDate joinedAt;

  /**
   * 보호소
   */
  private final ShelterDto shelter;

  /**
   * 팔로우 여부
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final Boolean isFollowingUser;

  public static UserProfileDto of(User user, Follow follow) {
    return UserProfileDto.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .profileImgUrl(user.getProfileImageUrl())
        .nickname(user.getNickname())
        .introduce(user.getIntroduce())
        .joinedAt(user.getCreatedAt().toLocalDate())
        .shelter(getShelterDto(user.getShelter()))
        .isFollowingUser(follow != null)
        .build();
  }

  public static UserProfileDto fromEntity(User user) {
    return UserProfileDto.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .profileImgUrl(user.getProfileImageUrl())
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
