package homes.banzzokee.domain.chat.dto;

import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.domain.room.dto.ChatUserDto;
import homes.banzzokee.domain.type.MessageType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅 조회 dto
 */
@Builder
@Getter
@AllArgsConstructor
public class MessageDto {

  private final ChatUserDto user;

  private final String message;

  private final MessageType messageType;

  private final LocalDateTime createdAt;

  public static MessageDto fromEntity(ChatMessage chatMessage) {
    return MessageDto.builder()
        .user(ChatUserDto.fromEntity(chatMessage.getUser()))
        .message(chatMessage.getMessage())
        .messageType(chatMessage.getMessageType())
        .createdAt(chatMessage.getCreatedAt())
        .build();
  }

}
