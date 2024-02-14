package homes.banzzokee.domain.adoption.elasticsearch.document.subclass;

import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.entity.User;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Builder
public class UserDocumentVo {

  private final Long userId;

  private final String email;

  private final String profileImgUrl;

  private final String nickname;

  private final String introduce;

  @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
  private final LocalDate joinedAt;

  private final ShelterDocumentVo shelter;

  public static UserDocumentVo fromEntity(User user) {
    return UserDocumentVo.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .profileImgUrl(user.getProfileImageUrl())
        .nickname(user.getNickname())
        .introduce(user.getIntroduce())
        .joinedAt(user.getCreatedAt().toLocalDate())
        .shelter(getShelterDocumentVo(user.getShelter()))
        .build();
  }

  private static ShelterDocumentVo getShelterDocumentVo(Shelter shelter) {
    if (shelter != null && shelter.isVerified()) {
      return ShelterDocumentVo.fromEntity(shelter);
    }
    return null;
  }
}
