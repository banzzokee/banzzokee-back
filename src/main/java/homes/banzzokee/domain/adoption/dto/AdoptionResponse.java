package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.review.dto.ReviewDto;
import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

  private final BreedType breed;

  private final DogSize size;

  private final boolean neutering;

  private final DogGender gender;

  private final int age;

  private final boolean healthChecked;

  private final LocalDate registeredAt;

  private final AdoptionStatus status;

  private final LocalDate adoptedAt;

  private final LocalDateTime createdAt;

  private final LocalDateTime updatedAt;

  private final ReviewDto review;

  private boolean isBookmarked;

  private boolean isFollowed;

  public static AdoptionResponse fromEntity(Adoption adoption) {
    return AdoptionResponse.builder()
        .adoptionId(adoption.getId())
        .user(getUser(adoption.getUser()))
        .assignedUser(getUser(adoption.getAssignedUser()))
        .title(adoption.getTitle())
        .content(adoption.getContent())
        .imageUrls(getImages(adoption.getImages()))
        .breed(adoption.getBreed())
        .size(adoption.getSize())
        .neutering(adoption.isNeutering())
        .gender(adoption.getGender())
        .age(adoption.getAge())
        .healthChecked(adoption.isHealthChecked())
        .registeredAt(adoption.getRegisteredAt())
        .status(adoption.getStatus())
        .adoptedAt(adoption.getAdoptedAt())
        .createdAt(adoption.getCreatedAt())
        .updatedAt(adoption.getUpdatedAt())
        .review(getReview(adoption.getReview()))
        .isBookmarked(false)
        .isFollowed(false)
        .build();
  }

  private static ReviewDto getReview(Review review) {
    if (review == null) {
      return null;
    }
    return ReviewDto.fromEntity(review);
  }

  // assignedUser는 null이 가능하여 별도 null 처리
  private static UserProfileDto getUser(User user) {
    if (user == null) {
      return null;
    }
    return UserProfileDto.fromEntity(user);
  }

  // 아래 사항들은 현재 null 값을 인정하지 않지만, 향후 운영정책 변경 시 null 처리 가능하게 하도록 작성함
  // (status는 null이면 안 된다고 판단하여 작성하지 않음)
  private static List<String> getImages(List<S3Object> images) {
    if (images == null) {
      return null;
    }
    return images.stream().map(S3Object::getUrl).toList();
  }

  public void updateIsBookmarkedAndIsFollowed(boolean isBookmarked, boolean isFollowed) {
    this.isBookmarked = isBookmarked;
    this.isFollowed = isFollowed;
  }
}
