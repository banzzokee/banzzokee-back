package homes.banzzokee.domain.review.dto;

import homes.banzzokee.domain.adoption.elasticsearch.document.AdoptionDocument;
import homes.banzzokee.domain.adoption.elasticsearch.document.subclass.ReviewDocumentVo;
import homes.banzzokee.domain.type.S3Object;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewSearchResponse {

  private final long reviewId;

  private final long userId;

  private final String userNickname;

  private final String title;

  private final String content;

  private final List<String> imageUrls;

  private final LocalDateTime createdAt;

  private final LocalDateTime updatedAt;

  public static ReviewSearchResponse fromDocument(AdoptionDocument adoptionDocument) {
    ReviewDocumentVo reviewDocumentVo = adoptionDocument.getReview();
    return ReviewSearchResponse.builder()
        .reviewId(reviewDocumentVo.getReviewId())
        .userId(reviewDocumentVo.getUserId())
        .userNickname(reviewDocumentVo.getUserNickname())
        .title(reviewDocumentVo.getTitle())
        .content(reviewDocumentVo.getContent())
        .imageUrls(getImages(reviewDocumentVo))
        .createdAt(reviewDocumentVo.getCreatedAt())
        .updatedAt(reviewDocumentVo.getUpdatedAt())
        .build();
  }

  private static List<String> getImages(ReviewDocumentVo reviewDocumentVo) {
    if (reviewDocumentVo.getImages() == null) {
      return null;
    }
    return reviewDocumentVo.getImages().stream().map(S3Object::getUrl).toList();
  }

}
