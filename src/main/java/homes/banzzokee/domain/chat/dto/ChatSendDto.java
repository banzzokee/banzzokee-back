package homes.banzzokee.domain.chat.dto;

import homes.banzzokee.domain.type.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅 전송 Request dto
 */
@Getter
@Builder
@AllArgsConstructor
public class ChatSendDto {

  private final String message;

  private final MessageType messageType;

}
