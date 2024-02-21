package homes.banzzokee.domain.chat.controller;

import homes.banzzokee.domain.chat.dto.ChatSendDto;
import homes.banzzokee.domain.chat.dto.MessageDto;
import homes.banzzokee.domain.chat.service.ChatMessageService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

/**
 * STOMP message 전송
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MessageController {

  private final ChatMessageService chatMessageService;

  /**
   * 채팅 전송
   *
   * @param roomId
   * @param message
   * @return
   */
  @MessageMapping("/chats/rooms/{roomId}")
  // /api/chats/rooms/{roomId} 로 메세지 전송 요청
  @SendTo("/topic/chats.rooms.{roomId}")  // subscribe destination
  public MessageDto sendMessage(
      Principal principal,
      @DestinationVariable("roomId") Long roomId,
      @Payload ChatSendDto message) {

    log.info("[sendMessage] room id : {}, user-id: {}", roomId,
        principal.getName());

    return chatMessageService.sendMessage(principal.getName(), roomId, message);
  }

}
