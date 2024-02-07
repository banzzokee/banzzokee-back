package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.type.S3Object;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@AllArgsConstructor
@Builder
public class AdoptionDto {

  private final Long adoptionId;

  private final String title;

  private final String content;

  private final List<S3Object> imageUrls;

  private final String breed;

  private final String size;

  private final boolean neutering;

  private final String gender;

  private final int age;

  private final boolean healthChecked;

  @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
  private final LocalDate registeredAt;

  @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
  private final LocalDate adoptedAt;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private final LocalDateTime createdAt;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private final LocalDateTime updatedAt;

  public static AdoptionDto fromEntity(Adoption adoption) {
    return AdoptionDto.builder()
        .adoptionId(adoption.getId())
        .title(adoption.getTitle())
        .content(adoption.getContent())
        .imageUrls(adoption.getImages())
        .breed(adoption.getBreed() == null ? null : adoption.getBreed().getBreed())
        .size(adoption.getSize() == null ? null : adoption.getSize().getSize())
        .neutering(adoption.isNeutering())
        .gender(adoption.getGender() == null ? null : adoption.getGender().getGender())
        .age(adoption.getAge())
        .healthChecked(adoption.isHealthChecked())
        .registeredAt(adoption.getRegisteredAt())
        .adoptedAt(adoption.getAdoptedAt())
        .createdAt(adoption.getCreatedAt())
        .updatedAt(adoption.getUpdatedAt())
        .build();
  }

}
