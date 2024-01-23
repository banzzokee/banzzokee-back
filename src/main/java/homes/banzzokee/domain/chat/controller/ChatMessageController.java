package homes.banzzokee.domain.chat.controller;

import homes.banzzokee.domain.chat.dto.MessageDto;
import homes.banzzokee.domain.chat.service.ChatMessageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채팅 메세지 api
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatMessageController {

  private final ChatMessageService chatMessageService;

  /**
   * 채팅 목록 조회
   * @param roomId
   * @return
   */
  @GetMapping("/chats/rooms/{roomId}")
  public List<MessageDto> getChatList(@PathVariable("roomId") Long roomId) {

    return chatMessageService.getChatList(roomId);

  }

}
