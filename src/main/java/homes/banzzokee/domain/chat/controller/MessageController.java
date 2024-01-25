package homes.banzzokee.domain.chat.controller;

import homes.banzzokee.domain.chat.dto.MessageDto;
import homes.banzzokee.domain.chat.dto.SendChatDto;
import homes.banzzokee.domain.chat.service.ChatMessageService;
import homes.banzzokee.global.config.stomp.exception.SocketException;
import homes.banzzokee.global.error.ErrorResponse;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
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
  public MessageDto sendMessage(
      Principal principal,
      @DestinationVariable("roomId") Long roomId,
      @Payload SendChatDto message) {

    log.info("[sendMessage] room id : {}, user-id: '{}'", roomId,
        principal.getName());

    return chatMessageService.sendMessage(roomId, message);
  }

  @MessageExceptionHandler  // 메세지 전송 에러 핸들러
  @SendToUser("/queue/error") // 특정 유저에게 메세지 전송 ->
                              // "/user/queue/error" 구독한 유저
  public ResponseEntity<ErrorResponse> handleException(
      Principal principal,  // 쓰이지 않더라도 파라미터로 받아와야 특정한 유저에게 보낼 수 있음
      SocketException e) {

    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(
            ErrorResponse.of(e.getErrorCode())
        );
  }

}
