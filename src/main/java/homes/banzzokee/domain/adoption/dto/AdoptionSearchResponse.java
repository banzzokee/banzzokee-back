package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
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
@Builder
@AllArgsConstructor
public class AdoptionSearchResponse {

  private final long adoptionId;

  private final long userId;

  private final String userNickname;

  private final Long assignedUserId;

  private final String title;

  private final String content;

  private final List<String> imageUrls;

  private final BreedType breed;

  private final DogSize size;

  private final boolean neutering;

  private final DogGender gender;

  private final int age;

  private final boolean healthChecked;

  private final LocalDate registeredAt;

  private final AdoptionStatus status;

  private final LocalDate adoptedAt;

  private final LocalDateTime updatedAt;

  private final LocalDateTime createdAt;

  private final Long reviewId;

  public static AdoptionSearchResponse fromDocument(AdoptionDocument document) {
    return AdoptionSearchResponse.builder()
        .adoptionId(document.getId())
        .userId(document.getUserId())
        .userNickname(document.getUserNickname())
        .assignedUserId(
            document.getAssignedUserId() == null ? null : document.getAssignedUserId())
        .title(document.getTitle())
        .content(document.getContent())
        .imageUrls(document.getImages() == null ? null
            : document.getImages().stream().map(S3Object::getUrl).toList())
        .breed(document.getBreed())
        .size(document.getSize())
        .neutering(document.isNeutering())
        .gender(document.getGender())
        .age(document.getAge())
        .healthChecked(document.isHealthChecked())
        .registeredAt(document.getRegisteredAt())
        .status(document.getStatus())
        .adoptedAt(document.getAdoptedAt() == null ? null : document.getAdoptedAt())
        .updatedAt(document.getUpdatedAt() == null ? null : document.getUpdatedAt())
        .createdAt(document.getCreatedAt())
        .reviewId(
            document.getReview() == null ? null : document.getReview().getReviewId())
        .build();
  }

}
