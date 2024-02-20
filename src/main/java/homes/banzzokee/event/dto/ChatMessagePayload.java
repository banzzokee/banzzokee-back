package homes.banzzokee.event.dto;

import homes.banzzokee.domain.type.MessageType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessagePayload {

  private final Long messageId;
  private final Long roomId;
  private final String message;
  private final MessageType messageType;
  private final Long senderId;
  private final String sender;
  private final Long receiverId;
  private final String receiver;
}
