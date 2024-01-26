package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record AdoptionResponse (
  Long adoptionId,
  UserProfileDto user,
  UserProfileDto assignedUser,
  String title,
  String content,
  List<String> imageUrls,
  String breed,
  String size,
  boolean neutering,
  String gender,
  int age,
  boolean healthChecked,
  LocalDate registeredAt,
  String status,
  LocalDate adoptedAt,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
//  ReviewDto review
) {
  public static AdoptionResponse fromEntity(Adoption adoption) {
    return AdoptionResponse.builder()
        .adoptionId(adoption.getId())
        .user(UserProfileDto.fromEntity(adoption.getUser()))
        .assignedUser(UserProfileDto.fromEntity(adoption.getAssignedUser()))
        .title(adoption.getTitle())
        .content(adoption.getContent())
        .imageUrls(adoption.getImages())
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
//        .review(ReviewDto.fromEntity(adoption.getReview()))
        .build();
  }
}
