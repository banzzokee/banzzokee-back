package homes.banzzokee.domain.chat.dto;

import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.domain.room.dto.ChatUserDto;
import homes.banzzokee.domain.type.MessageType;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * 채팅 조회 dto
 *
 * @param user
 * @param message
 * @param messageType
 * @param createdAt
 */
@Builder
public record MessageDto(
    ChatUserDto user,
    String message,
    MessageType messageType,
    LocalDateTime createdAt
) {

  public static MessageDto fromEntity(ChatMessage chatMessage) {
    return MessageDto.builder()
        .user(ChatUserDto.fromEntity(chatMessage.getUser()))
        .message(chatMessage.getMessage())
        .messageType(chatMessage.getMessageType())
        .createdAt(chatMessage.getCreatedAt())
        .build();
  }

}
