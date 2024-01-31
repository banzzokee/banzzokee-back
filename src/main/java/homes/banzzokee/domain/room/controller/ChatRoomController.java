package homes.banzzokee.domain.room.controller;

import homes.banzzokee.domain.room.dto.ChatRoomDto;
import homes.banzzokee.domain.room.dto.RoomCreateResponse;
import homes.banzzokee.domain.room.service.ChatRoomService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
  public RoomCreateResponse createChatRoom(
//      @AuthenticationPrincipal UserDetails,
      @PathVariable("adoptionId") Long adoptionId) {

    log.info("[createChatRoom] 채팅방 생성 요청 - 입양글 id : {}", adoptionId);

    // todo: userId 삭제
    // todo: "banzzokee" -> userDetails.username()
    return chatRoomService.createChatRoom("banzzokee", adoptionId, 1L);

  }

  /**
   * 채팅방 목록 조회
   *
   * @param email
   * @return
   */
  @GetMapping("")
  public List<ChatRoomDto> getChatRooms(
//      @AuthenticationPrincipal UserDetails,
      @RequestParam("email") String email) {

    log.info("[getChatRooms] 채팅방 목록 조회 요청 - 유저 email : {}", email);

    return chatRoomService.getChatRooms(email);
  }
}
