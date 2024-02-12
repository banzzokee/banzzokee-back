package homes.banzzokee.domain.review.dto;

import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.type.S3Object;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReviewDto {

  private final Long reviewId;

  private final String title;

  private final String content;

  private final List<String> images;

  public static ReviewDto fromEntity(Review review) {
    return ReviewDto.builder()
        .reviewId(review.getId())
        .title(review.getTitle())
        .content(review.getContent())
        .images(getImages(review))
        .build();
  }

  private static List<String> getImages(Review review) {
    if (review.getImages() == null) {
      return null;
    }
    return review.getImages().stream().map(S3Object::getUrl).toList();
  }
}
