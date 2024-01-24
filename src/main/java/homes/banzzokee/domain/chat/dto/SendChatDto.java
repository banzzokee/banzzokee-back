package homes.banzzokee.domain.chat.dto;

import homes.banzzokee.domain.type.MessageType;

/**
 * 채팅 전송 Request dto
 *
 * @param message
 * @param messageType
 */
public record SendChatDto(
    String message,
    MessageType messageType
) {

}
