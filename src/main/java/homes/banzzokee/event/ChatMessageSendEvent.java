package homes.banzzokee.event;

import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.event.dto.ChatMessagePayload;
import homes.banzzokee.infra.rabbitmq.event.BaseMessage;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
public class ChatMessageSendEvent extends BaseMessage<ChatMessagePayload> {

  public static ChatMessageSendEvent from(ChatMessage chatMessage) {
    return ChatMessageSendEvent.builder()
        .routingKey("message.send")
        .payload(ChatMessagePayload.builder()
            .messageId(chatMessage.getId())
            .roomId(chatMessage.getRoom().getId())
            .message(chatMessage.getMessage())
            .messageType(chatMessage.getMessageType())
            .senderId(chatMessage.getUser().getId())
            .sender(chatMessage.getUser().getNickname())
            .receiverId(chatMessage.getReceiver().getId())
            .receiver(chatMessage.getReceiver().getNickname())
            .build())
        .build();
  }
}