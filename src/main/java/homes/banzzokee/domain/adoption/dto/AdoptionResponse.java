package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import java.time.format.DateTimeFormatter;
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
  String registeredAt,
  String status,
  String adoptedAt,
  String createdAt,
  String updatedAt
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
        .status(adoption.getStatus().getStatus())
        .adoptedAt(adoption.getAdoptedAt().format(DateTimeFormatter.ISO_DATE))
        .createdAt(adoption.getCreatedAt().format(DateTimeFormatter.ISO_DATE))
        .updatedAt(adoption.getUpdatedAt().format(DateTimeFormatter.ISO_DATE))
//        .review(ReviewDto.fromEntity(adoption.getReview()))
        .build();
  }
}
