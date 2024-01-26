package homes.banzzokee.domain.room.dto;

import homes.banzzokee.domain.shelter.entity.Shelter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅 보호소
 */
@Builder
@Getter
@AllArgsConstructor
public class ChatShelterDto {

  private final Long shelterId;

  private final String shelterImgUrl;

  private final String name;
  public static ChatShelterDto fromEntity(Shelter shelter) {
    return ChatShelterDto.builder()
        .shelterId(shelter.getId())
        .shelterImgUrl(shelter.getShelterImgUrl())
        .name(shelter.getName())
        .build();
  }

}
