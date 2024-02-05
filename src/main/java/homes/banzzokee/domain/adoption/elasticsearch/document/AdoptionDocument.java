package homes.banzzokee.domain.adoption.elasticsearch.document;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dto.UserProfileDto;
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
@Document(indexName = "adoption", writeTypeHint = WriteTypeHint.FALSE)
@Setting(settingPath = "/elasticsearch/adoption-setting.json")
@Mapping(mappingPath = "/elasticsearch/adoption-mapping.json")
public class AdoptionDocument {

  @Id
  private Long id;

  private String title;

  private String content;

  private String status;

  private String breed;

  private String size;

  private boolean neutering;

  private String gender;

  private int age;

  private boolean healthChecked;

  @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
  private LocalDate registeredAt;

  private List<S3Object> images;

  @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
  private LocalDate adoptedAt;

  private UserProfileDto user;

  private UserProfileDto assignedUser;

  // Todo : private ReviewDto review;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private LocalDateTime deletedAt;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private LocalDateTime createdAt;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private LocalDateTime updatedAt;

  public static AdoptionDocument fromEntity(Adoption adoption) {
    return AdoptionDocument.builder()
        .id(adoption.getId())
        .title(adoption.getTitle())
        .content(adoption.getContent())
        .status(adoption.getStatus().getStatus())
        .breed(adoption.getBreed().getBreed())
        .size(adoption.getSize().getSize())
        .neutering(adoption.isNeutering())
        .gender(adoption.getGender().getGender())
        .age(adoption.getAge())
        .healthChecked(adoption.isHealthChecked())
        .registeredAt(adoption.getRegisteredAt())
        .images(adoption.getImages())
        .user(UserProfileDto.fromEntity(adoption.getUser()))
        .assignedUser(
            adoption.getAssignedUser() == null ? null
                : UserProfileDto.fromEntity(adoption.getAssignedUser()))
        .deletedAt(adoption.getDeletedAt())
        .createdAt(adoption.getCreatedAt())
        .updatedAt(adoption.getUpdatedAt())
        .build();
  }

  public void update(Adoption adoption) {
    this.title = adoption.getTitle();
    this.content = adoption.getContent();
    this.breed = adoption.getBreed().getBreed();
    this.size = adoption.getSize().getSize();
    this.neutering = adoption.isNeutering();
    this.gender = adoption.getGender().getGender();
    this.age = adoption.getAge();
    this.healthChecked = adoption.isHealthChecked();
    this.registeredAt = adoption.getRegisteredAt();
    this.images = adoption.getImages();
    this.updatedAt = adoption.getUpdatedAt();
  }

  public void updateStatus(Adoption adoption) {
    this.status = adoption.getStatus().getStatus();
    this.assignedUser = getUserProfileDto(adoption);
    this.adoptedAt = adoption.getAdoptedAt();
    this.updatedAt = adoption.getUpdatedAt();
  }

  private UserProfileDto getUserProfileDto(Adoption adoption) {
    if (adoption.getAssignedUser() == null) {
      return null;
    }
    return UserProfileDto.fromEntity(adoption.getAssignedUser());
  }

  public void delete(Adoption adoption) {
    this.deletedAt = adoption.getDeletedAt();
  }
}
