package homes.banzzokee.domain.adoption.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.bookmark.entity.Bookmark;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.domain.type.S3Object;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AdoptionDto {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final Long bookmarkId;

  private final Long adoptionId;

  private final Long userId;

  private final String userNickname;

  private final String title;

  private final String content;

  private final List<String> imageUrls;

  private final AdoptionStatus status;

  private final BreedType breed;

  private final DogSize size;

  private final boolean neutering;

  private final DogGender gender;

  private final int age;

  private final boolean healthChecked;

  private final LocalDate registeredAt;

  private final LocalDate adoptedAt;

  private final LocalDateTime createdAt;

  private final LocalDateTime updatedAt;

  public static AdoptionDto fromEntity(Adoption adoption) {
    return AdoptionDto.builder()
        .adoptionId(adoption.getId())
        .userId(adoption.getUser().getId())
        .userNickname(adoption.getUser().getNickname())
        .title(adoption.getTitle())
        .content(adoption.getContent())
        .imageUrls(getImageUrlList(adoption))
        .status(adoption.getStatus())
        .breed(adoption.getBreed())
        .size(adoption.getSize())
        .neutering(adoption.isNeutering())
        .gender(adoption.getGender())
        .age(adoption.getAge())
        .healthChecked(adoption.isHealthChecked())
        .registeredAt(adoption.getRegisteredAt())
        .adoptedAt(adoption.getAdoptedAt())
        .createdAt(adoption.getCreatedAt())
        .updatedAt(adoption.getUpdatedAt())
        .build();
  }

  public static AdoptionDto of(Adoption adoption, Bookmark bookmark) {
    return AdoptionDto.builder()
        .bookmarkId(bookmark.getId())
        .adoptionId(adoption.getId())
        .userId(adoption.getUser().getId())
        .userNickname(adoption.getUser().getNickname())
        .title(adoption.getTitle())
        .content(adoption.getContent())
        .imageUrls(getImageUrlList(adoption))
        .status(adoption.getStatus())
        .breed(adoption.getBreed())
        .size(adoption.getSize())
        .neutering(adoption.isNeutering())
        .gender(adoption.getGender())
        .age(adoption.getAge())
        .healthChecked(adoption.isHealthChecked())
        .registeredAt(adoption.getRegisteredAt())
        .adoptedAt(adoption.getAdoptedAt())
        .createdAt(adoption.getCreatedAt())
        .updatedAt(adoption.getUpdatedAt())
        .build();
  }

  private static List<String> getImageUrlList(Adoption adoption) {
    if (adoption.getImages() == null) {
      return null;
    }
    return adoption.getImages().stream().map(S3Object::getUrl).toList();
  }
}
