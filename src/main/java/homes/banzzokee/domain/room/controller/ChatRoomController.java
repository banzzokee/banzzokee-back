package homes.banzzokee.domain.room.controller;

import homes.banzzokee.domain.room.dto.CreateRoomResponse;
import homes.banzzokee.domain.room.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채팅방 api
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;

  /**
   * 채팅방 생성
   *
   * @param adoptionId
   * @return
   */
  @PostMapping("/adoptions/{adoptionId}")
  public CreateRoomResponse createChatRoom(
//      @AuthenticationPrincipal UserDetails,
      @PathVariable("adoptionId") Long adoptionId) {

    log.info("[createChatRoom] 채팅방 생성 요청 - 입양글 id : {}", adoptionId);

    // todo: userId 삭제
    // todo: "banzzokee" -> userDetails.username()
    return chatRoomService.createChatRoom("banzzokee", adoptionId, 1L);

  }
}
