package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.domain.adoption.entity.Adoption;
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

  private final Long adoptionId;

  private final Long userId;

  private final String userNickname;

  private final String title;

  private final String content;

  private final List<S3Object> imageUrls;

  private final String status;

  private final String breed;

  private final String size;

  private final boolean neutering;

  private final String gender;

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
        .imageUrls(adoption.getImages())
        .status(adoption.getStatus() == null ? null : adoption.getStatus().getStatus())
        .breed(adoption.getBreed() == null ? null : adoption.getBreed().getBreed())
        .size(adoption.getSize() == null ? null : adoption.getSize().getSize())
        .neutering(adoption.isNeutering())
        .gender(adoption.getGender() == null ? null : adoption.getGender().getGender())
        .age(adoption.getAge())
        .healthChecked(adoption.isHealthChecked())
        .registeredAt(adoption.getRegisteredAt())
        .adoptedAt(adoption.getAdoptedAt())
        .createdAt(adoption.getCreatedAt())
        .updatedAt(adoption.getUpdatedAt())
        .build();
  }
}
