package homes.banzzokee.domain.review.elasticsearch.document;

import homes.banzzokee.domain.adoption.elasticsearch.document.subclass.UserDocumentVo;
import homes.banzzokee.domain.review.elasticsearch.document.subclass.AdoptionDocumentVo;
import homes.banzzokee.domain.review.entity.Review;
import homes.banzzokee.domain.type.S3Object;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

@Getter
@Builder
@AllArgsConstructor
@Document(indexName = "review", writeTypeHint = WriteTypeHint.FALSE)
@Setting(settingPath = "/elasticsearch/setting.json")
@Mapping(mappingPath = "/elasticsearch/review-mapping.json")
public class ReviewDocument {

  @Id
  private Long id;

  private AdoptionDocumentVo adoption;

  private UserDocumentVo user;

  private String title;

  private String content;

  private List<S3Object> images;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private LocalDateTime deletedAt;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private LocalDateTime createdAt;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private LocalDateTime updatedAt;

  public static ReviewDocument fromEntity(Review review) {
    return ReviewDocument.builder()
        .id(review.getId())
        .adoption(AdoptionDocumentVo.fromEntity(review.getAdoption()))
        .user(UserDocumentVo.fromEntity(review.getUser()))
        .title(review.getTitle())
        .content(review.getContent())
        .images(review.getImages())
        .deletedAt(review.getDeletedAt())
        .createdAt(review.getCreatedAt())
        .updatedAt(review.getUpdatedAt())
        .build();
  }

  public void update(String title, String content, List<S3Object> images) {
    this.title = title;
    this.content = content;
    this.images = images;
  }

  public void delete(LocalDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }
}
