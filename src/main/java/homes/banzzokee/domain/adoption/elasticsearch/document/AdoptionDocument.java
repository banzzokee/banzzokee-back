package homes.banzzokee.domain.adoption.elasticsearch.document;

import homes.banzzokee.domain.adoption.elasticsearch.document.subclass.ReviewDocumentVo;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.entity.User;
import jakarta.persistence.Id;
import java.time.LocalDate;
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
@Document(indexName = "adoptions", writeTypeHint = WriteTypeHint.FALSE)
@Setting(settingPath = "/elasticsearch/setting.json")
@Mapping(mappingPath = "/elasticsearch/adoption-mapping.json")
public class AdoptionDocument {

  @Id
  private Long id;

  private Long userId;

  private String userNickname;

  private Long assignedUserId;

  private String title;

  private String content;

  private List<S3Object> images;

  private BreedType breed;

  private DogSize size;

  private boolean neutering;

  private DogGender gender;

  private int age;

  private boolean healthChecked;

  private AdoptionStatus status;

  private ReviewDocumentVo review;

  @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
  private LocalDate registeredAt;

  @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
  private LocalDate adoptedAt;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private LocalDateTime createdAt;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private LocalDateTime updatedAt;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private LocalDateTime deletedAt;

  public static AdoptionDocument fromEntity(Adoption adoption) {
    return AdoptionDocument.builder()
        .id(adoption.getId())
        .userId(adoption.getUser().getId())
        .userNickname(adoption.getUser().getNickname())
        .assignedUserId(registerAssignedUserId(adoption.getAssignedUser()))
        .title(adoption.getTitle())
        .content(adoption.getContent())
        .images(adoption.getImages())
        .breed(adoption.getBreed())
        .size(adoption.getSize())
        .neutering(adoption.isNeutering())
        .gender(adoption.getGender())
        .age(adoption.getAge())
        .healthChecked(adoption.isHealthChecked())
        .registeredAt(adoption.getRegisteredAt())
        .status(adoption.getStatus())
        .adoptedAt(adoption.getAdoptedAt())
        .updatedAt(adoption.getUpdatedAt())
        .createdAt(adoption.getCreatedAt())
        .review(getReview(adoption))
        .deletedAt(adoption.getDeletedAt())
        .build();
  }

  public void update(Adoption adoption) {
    this.title = adoption.getTitle();
    this.content = adoption.getContent();
    this.breed = adoption.getBreed();
    this.size = adoption.getSize();
    this.neutering = adoption.isNeutering();
    this.gender = adoption.getGender();
    this.age = adoption.getAge();
    this.healthChecked = adoption.isHealthChecked();
    this.registeredAt = adoption.getRegisteredAt();
    this.images = adoption.getImages();
    this.updatedAt = adoption.getUpdatedAt();
  }

  public void updateStatus(Adoption adoption) {
    this.status = adoption.getStatus();
    this.assignedUserId = registerAssignedUserId(adoption.getAssignedUser());
    this.adoptedAt = adoption.getAdoptedAt();
    this.updatedAt = adoption.getUpdatedAt();
  }

  public void updateReview(ReviewDocumentVo review) {
    this.review = review;
  }

  private static Long registerAssignedUserId(User user) {
    if (user == null) {
      return null;
    }
    return user.getId();
  }

  private static ReviewDocumentVo getReview(Adoption adoption) {
    if (adoption.getReview() == null) {
      return null;
    }
    return ReviewDocumentVo.fromEntity(adoption.getReview());
  }

  public void delete(Adoption adoption) {
    this.deletedAt = adoption.getDeletedAt();
  }

  public void deleteReview() {
    this.review = null;
  }
}
