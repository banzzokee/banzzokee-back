package homes.banzzokee.domain.room.dto;

import homes.banzzokee.domain.room.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅방 생성 Response
 */
@Builder
@Getter
@AllArgsConstructor
public class CreateRoomResponse {

  private final Long roomId;

  private final ChatUserDto user;

  private final ChatShelterDto shelter;

  public static CreateRoomResponse fromEntity(ChatRoom chatRoom) {
    return CreateRoomResponse.builder()
        .roomId(chatRoom.getId())
        .user(ChatUserDto.fromEntity(chatRoom.getUser()))
        .shelter(ChatShelterDto.fromEntity(chatRoom.getShelter()))
        .build();
  }

}
