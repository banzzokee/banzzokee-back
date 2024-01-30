package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class AdoptionResponse {

  private final Long adoptionId;

  private final UserProfileDto user;

  private final UserProfileDto assignedUser;

  private final String title;

  private final String content;

  private final List<String> imageUrls;

  private final String breed;

  private final String size;

  private final boolean neutering;

  private final String gender;

  private final int age;

  private final boolean healthChecked;

  private final LocalDate registeredAt;

  private final String status;

  private final LocalDate adoptedAt;

  private final LocalDateTime createdAt;

  private final LocalDateTime updatedAt;

  //  ReviewDto review

  public static AdoptionResponse fromEntity(Adoption adoption) {
    return AdoptionResponse.builder()
        .adoptionId(adoption.getId())
        .user(UserProfileDto.fromEntity(adoption.getUser()))
        .assignedUser(UserProfileDto.fromEntity(adoption.getAssignedUser()))
        .title(adoption.getTitle())
        .content(adoption.getContent())
        .imageUrls(Collections.unmodifiableList(adoption.getImages().stream().map(
            S3Object::getUrl).collect(Collectors.toList())))
        .breed(adoption.getBreed().getBreed())
        .size(adoption.getSize().getSize())
        .neutering(adoption.isNeutering())
        .gender(adoption.getGender().getGender())
        .age(adoption.getAge())
        .healthChecked(adoption.isHealthChecked())
        .registeredAt(adoption.getRegisteredAt())
        .status(adoption.getStatus().getStatus())
        .adoptedAt(adoption.getAdoptedAt())
        .createdAt(adoption.getCreatedAt())
        .updatedAt(adoption.getUpdatedAt())
// Todo:  .review(ReviewDto.fromEntity(adoption.getReview()))
        .build();
  }
}
