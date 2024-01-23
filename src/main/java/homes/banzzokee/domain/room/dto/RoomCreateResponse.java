package homes.banzzokee.domain.room.dto;

import homes.banzzokee.domain.room.entity.ChatRoom;
import lombok.Builder;

/**
 * 채팅방 생성 Response
 *
 * @param roomId
 * @param user
 * @param shelter
 */
@Builder
public record RoomCreateResponse(
    Long roomId,
    ChatUserDto user,
    ChatShelterDto shelter
) {

  public static RoomCreateResponse fromEntity(ChatRoom chatRoom) {
    return RoomCreateResponse.builder()
        .roomId(chatRoom.getId())
        .user(ChatUserDto.fromEntity(chatRoom.getUser()))
        .shelter(ChatShelterDto.fromEntity(chatRoom.getShelter()))
        .build();
  }

}
