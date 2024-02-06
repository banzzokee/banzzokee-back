package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
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

  private final String breed;

  private final String size;

  private final boolean neutering;

  private final String gender;

  private final int age;

  private final boolean healthChecked;

  private final LocalDate registeredAt;

  private final String status;

  private final LocalDate adoptedAt;

  private final LocalDateTime updatedAt;

  private final LocalDateTime createdAt;

  private final Long reviewId;

  public static AdoptionSearchResponse fromDocument(AdoptionDocument document) {
    return AdoptionSearchResponse.builder()
        .adoptionId(document.getId())
        .userId(document.getUser().getUserId())
        .userNickname(document.getUser().getNickname())
        .assignedUserId(document.getAssignedUser() == null ? null
            : document.getAssignedUser().getUserId())
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
        // Todo : .reviewId(document.getReview() == null ? null : document.getReview().getId())
        .build();
  }

}
