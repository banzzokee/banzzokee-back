package homes.banzzokee.domain.room.dto;

import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.type.MessageType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅조회 dto
 */
@Getter
@Builder
@AllArgsConstructor
public class ChatRoomDto {

  /**
   * 채팅방 번호
   */
  private final Long roomId;

  /**
   * 입양글
   */
  private final ChatAdoptionDto adoption;

  /**
   * 조회하는 유저
   */
  private final ChatUserDto user;

  /**
   * 채팅방 마지막 채팅
   */
  private final String lastMessage;

  /**
   * 채팅방 마지막 채팅 타입
   */
  private final MessageType lastMessageType;

  /**
   * 채팅방 마지막 채팅 시간
   */
  private final LocalDateTime lastMessageCreatedAt;

  public static ChatRoomDto fromEntity(
      ChatRoom chatRoom) {

    return ChatRoomDto.builder()
        .roomId(chatRoom.getId())
        .adoption(ChatAdoptionDto.fromEntity(chatRoom.getAdoption()))
        .user(ChatUserDto.fromEntity(chatRoom.getUser()))
        .lastMessage(chatRoom.getLastMessage())
        .lastMessageType(chatRoom.getLastMessageType())
        .lastMessageCreatedAt(chatRoom.getLastMessageCreatedAt())
        .build();
  }
}
