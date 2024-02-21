package homes.banzzokee.domain.adoption.elasticsearch.document.subclass;

import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.type.S3Object;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Builder
public class ReviewDocumentVo {

  private final Long reviewId;

  private final long userId;

  private final String userNickname;

  private final String title;

  private final String content;

  private final List<S3Object> images;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private final LocalDateTime createdAt;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private final LocalDateTime updatedAt;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private final LocalDateTime deletedAt;

  public static ReviewDocumentVo fromEntity(Review review) {
    return ReviewDocumentVo.builder()
        .reviewId(review.getId())
        .userId(review.getUser().getId())
        .userNickname(review.getUser().getNickname())
        .title(review.getTitle())
        .content(review.getContent())
        .images(review.getImages())
        .createdAt(review.getCreatedAt())
        .updatedAt(review.getUpdatedAt())
        .deletedAt(review.getDeletedAt())
        .build();
  }

  public boolean isDeleted() {
    return this.deletedAt != null;
  }
}
