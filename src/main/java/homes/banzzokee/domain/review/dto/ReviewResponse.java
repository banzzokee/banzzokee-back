package homes.banzzokee.domain.review.dto;

import homes.banzzokee.domain.adoption.dto.AdoptionDto;
import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ReviewResponse {

  private final Long reviewId;

  private final AdoptionDto adoption;

  private final UserProfileDto user;

  private final String title;

  private final String content;

  private final List<String> images;

  public static ReviewResponse fromEntity(Review review) {
    return ReviewResponse.builder()
        .reviewId(review.getId())
        .adoption(getAdoption(review))
        .user(getUser(review))
        .title(review.getTitle())
        .content(review.getContent())
        .images(getImages(review))
        .build();
  }

  private static AdoptionDto getAdoption(Review review) {
    if (review.getAdoption() == null) {
      return null;
    }
    return AdoptionDto.fromEntity(review.getAdoption());
  }

  private static UserProfileDto getUser(Review review) {
    if (review.getUser() == null) {
      return null;
    }
    return UserProfileDto.fromEntity(review.getUser());
  }

  private static List<String> getImages(Review review) {
    if (review.getImages() == null) {
      return null;
    }
    return review.getImages().stream().map(S3Object::getUrl).toList();
  }

}
