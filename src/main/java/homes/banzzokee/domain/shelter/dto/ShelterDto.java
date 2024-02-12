package homes.banzzokee.domain.shelter.dto;

import homes.banzzokee.domain.shelter.entity.Shelter;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 보호소
 */
@Getter
@Builder
public class ShelterDto {

  private final Long shelterId;

  private final String shelterImgUrl;

  private final String name;

  private final String description;

  private final String tel;

  private final String address;

  @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
  private final LocalDate registeredAt;

  public static ShelterDto fromEntity(Shelter shelter) {
    return ShelterDto.builder()
        .shelterId(shelter.getId())
        .shelterImgUrl(shelter.getShelterImageUrl())
        .name(shelter.getName())
        .description(shelter.getDescription())
        .tel(shelter.getTel())
        .address(shelter.getAddress())
        .registeredAt(shelter.getCreatedAt().toLocalDate())
        .build();
  }
}
