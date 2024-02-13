package homes.banzzokee.domain.review.dto;

import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.type.S3Object;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewListResponse {

  private final long reviewId;

  private final long userId;

  private final String userNickname;

  private final String title;

  private final String content;

  private final List<String> imageUrls;

  private final LocalDateTime createdAt;

  private final LocalDateTime updatedAt;

  public static ReviewListResponse fromEntity(Review review) {
    return ReviewListResponse.builder()
        .reviewId(review.getId())
        .userId(review.getUser().getId())
        .userNickname(review.getUser().getNickname())
        .title(review.getTitle())
        .content(review.getContent())
        .imageUrls(getImageUrls(review.getImages()))
        .createdAt(review.getCreatedAt())
        .updatedAt(review.getUpdatedAt())
        .build();
  }

  private static List<String> getImageUrls(List<S3Object> images) {
    if (images == null) {
      return null;
    }
    return images.stream().map(S3Object::getUrl).toList();
  }

}
