package homes.banzzokee.domain.chat.controller;

import homes.banzzokee.domain.chat.dto.MessageDto;
import homes.banzzokee.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채팅 메세지 api
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatMessageController {

  private final ChatMessageService chatMessageService;

  /**
   * 채팅 목록 조회
   *
   * @param page
   * @param size
   * @param roomId
   * @return
   */
  @GetMapping("/rooms/{roomId}")
  public Slice<MessageDto> getChatList(
      @RequestParam("page") int page,
      @RequestParam("size") int size,
      @PathVariable("roomId") Long roomId) {

    log.info("[getChatList] 채팅방 채팅 목록 조회 요청");

    return chatMessageService.getChatList(
        roomId,
        PageRequest.of(page, size, Sort.by(Direction.DESC, "createdAt"))
    );

  }

}
