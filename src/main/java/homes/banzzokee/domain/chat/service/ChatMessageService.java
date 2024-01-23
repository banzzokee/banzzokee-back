package homes.banzzokee.domain.chat.service;

import static homes.banzzokee.domain.type.MessageType.ENTER;
import static homes.banzzokee.global.error.ErrorCode.FAILED;

import homes.banzzokee.domain.chat.dao.ChatMessageRepository;
import homes.banzzokee.domain.chat.dto.MessageDto;
import homes.banzzokee.domain.chat.dto.SendChatDto;
import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 채팅 메세지 service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

  private final UserRepository userRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;

  /**
   * 채팅 전송
   *
   * @param roomId
   * @param message
   * @return
   */
  public MessageDto sendMessage(Long roomId, SendChatDto message) {

    // todo: FAILED -> ROOM_NOT_FOUND
    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(FAILED));

    // todo: FAILED -> USER_NOT_FOUND
    // todo: @AuthenticationPrincipal username -> findByEmail(email) 로 변경
    User user = userRepository.findById(1L)
        .orElseThrow(() -> new CustomException(FAILED));

    String realMessage = message.messageType().equals(ENTER) ?
        user.getNickname() + " 님이 입장하였습니다." :
        message.message();

    return MessageDto.fromEntity(
        chatMessageRepository.save(ChatMessage.builder()
            .room(chatRoom)
            .user(user)
            .message(realMessage)
            .messageType(message.messageType())
            .build()
        )
    );
  }

  /**
   * 채팅 목록 조회
   *
   * @param roomId
   * @return
   */
  @Transactional(readOnly = true)
  public Slice<MessageDto> getChatList(Long roomId, Pageable pageable) {

    // todo: FAILED -> ROOM_NOT_FOUND
    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new CustomException(FAILED));

    return chatMessageRepository.findAllByRoom(chatRoom, pageable)
        .map(MessageDto::fromEntity);
  }

}
