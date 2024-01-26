package homes.banzzokee.domain.shelter.dto;

import homes.banzzokee.domain.shelter.entity.Shelter;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 보호소
 */
@Builder
@Getter
@AllArgsConstructor
public class ShelterDto {

  private final Long shelterId;

  private final String shelterImgUrl;

  private final String name;

  private final String description;

  private final String tel;

  private final String address;

  private final LocalDate registeredAt;

  public static ShelterDto fromEntity(Shelter shelter) {
    // TODO: shelter description 추가하기
    return ShelterDto.builder()
        .shelterId(shelter.getId())
        .shelterImgUrl(shelter.getShelterImgUrl())
        .name(shelter.getName())
        .description(null)
        .tel(shelter.getTel())
        .address(shelter.getAddress())
        .registeredAt(shelter.getCreatedAt().toLocalDate())
        .build();
  }
}
