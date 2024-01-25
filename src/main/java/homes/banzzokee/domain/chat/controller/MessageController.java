package homes.banzzokee.domain.chat.controller;

import homes.banzzokee.domain.chat.dto.MessageDto;
import homes.banzzokee.domain.chat.dto.SendChatDto;
import homes.banzzokee.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
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
  @SendTo("/queue/chats/rooms/{roomId}")  // subscribe destination
  public MessageDto sendMessage(@DestinationVariable("roomId") Long roomId,
      SendChatDto message) {
    log.info("[sendMessage] roomId : {}", roomId);

    return chatMessageService.sendMessage(roomId, message);
  }

}
