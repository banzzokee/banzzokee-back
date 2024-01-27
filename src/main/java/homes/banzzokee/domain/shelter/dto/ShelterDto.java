package homes.banzzokee.domain.shelter.dto;

import homes.banzzokee.domain.shelter.entity.Shelter;
import java.time.LocalDate;
import lombok.Builder;

/**
 * 보호소
 *
 * @param shelterId     보호소 아이디
 * @param shelterImgUrl 이미지 경로
 * @param name          이름
 * @param description   설명
 * @param tel           연락처
 * @param address       주소
 * @param registeredAt  등록일
 */
@Builder
public record ShelterDto(
    Long shelterId,
    String shelterImgUrl,
    String name,
    String description,
    String tel,
    String address,
    LocalDate registeredAt
) {

  public static ShelterDto fromEntity(Shelter shelter) {
    return ShelterDto.builder()
        .shelterId(shelter.getId())
        .shelterImgUrl(shelter.getShelterImgUrl())
        .name(shelter.getName())
        .description(shelter.getDescription())
        .tel(shelter.getTel())
        .address(shelter.getAddress())
        .registeredAt(shelter.getCreatedAt().toLocalDate())
        .build();
  }
}
