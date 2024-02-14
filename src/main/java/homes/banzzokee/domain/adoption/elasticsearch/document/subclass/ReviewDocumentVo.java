package homes.banzzokee.domain.adoption.elasticsearch.document.subclass;

import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.type.S3Object;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewDocumentVo {

  private final Long reviewId;

  private final String title;

  private final String content;

  private final List<String> images;

  public static ReviewDocumentVo fromEntity(Review review) {
    return ReviewDocumentVo.builder()
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
