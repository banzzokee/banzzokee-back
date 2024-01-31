package homes.banzzokee.domain.chat.dto;


import homes.banzzokee.domain.type.MessageType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LastChatMessageDto {

  private Long messageId;
  private String lastMessage;
  private MessageType lastMessageType;
  private LocalDateTime lastMessageCreatedAt;

}
