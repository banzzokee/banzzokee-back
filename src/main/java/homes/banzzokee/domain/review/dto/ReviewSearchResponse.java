package homes.banzzokee.domain.review.dto;

import homes.banzzokee.domain.review.elasticsearch.document.ReviewDocument;
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

  public static ReviewSearchResponse fromDocument(ReviewDocument reviewDocument) {
    return ReviewSearchResponse.builder()
        .reviewId(reviewDocument.getId())
        .userId(reviewDocument.getUser().getUserId())
        .userNickname(reviewDocument.getUser().getNickname())
        .title(reviewDocument.getTitle())
        .content(reviewDocument.getContent())
        .imageUrls(getImageUrls(reviewDocument.getImages()))
        .createdAt(reviewDocument.getCreatedAt())
        .updatedAt(reviewDocument.getUpdatedAt())
        .build();
  }

  private static List<String> getImageUrls(List<S3Object> images) {
    if (images == null) {
      return null;
    }
    return images.stream().map(S3Object::getUrl).toList();
  }

}
