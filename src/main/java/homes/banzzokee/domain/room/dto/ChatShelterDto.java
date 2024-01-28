package homes.banzzokee.domain.room.dto;

import homes.banzzokee.domain.shelter.entity.Shelter;
import lombok.Builder;

/**
 * 채팅 보호소
 *
 * @param shelterId
 * @param shelterImgUrl
 * @param name
 */
@Builder
public record ChatShelterDto(
    Long shelterId,
    String shelterImgUrl,
    String name
) {

  public static ChatShelterDto fromEntity(Shelter shelter) {
    return ChatShelterDto.builder()
        .shelterId(shelter.getId())
        .shelterImgUrl(shelter.getShelterImageUrl())
        .name(shelter.getName())
        .build();
  }

}
