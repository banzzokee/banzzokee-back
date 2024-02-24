package homes.banzzokee.domain.chat.service;

import static homes.banzzokee.global.error.ErrorCode.ROOM_NOT_FOUND;

import homes.banzzokee.domain.chat.dao.ChatMessageRepository;
import homes.banzzokee.domain.chat.dto.ChatSendDto;
import homes.banzzokee.domain.chat.dto.MessageDto;
import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.room.exception.SocketRoomNotFoundException;
import homes.banzzokee.domain.room.exception.SocketUserNotFoundException;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.event.ChatMessageSendEvent;
import homes.banzzokee.global.config.stomp.exception.SocketException;
import homes.banzzokee.global.config.stomp.exception.SocketNoAuthorizedException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ApplicationEventPublisher eventPublisher;

  /**
   * 채팅 전송
   *
   * @param roomId
   * @param message
   * @return
   */
  @Transactional
  public MessageDto sendMessage(String email, Long roomId, ChatSendDto message) {

    log.info("[sendMessage] 채팅 메세지 전송 유저 : {}, 채팅방 : {}", email, roomId);

    User user = userRepository.findByEmailAndDeletedAtNull(email)
        .orElseThrow(SocketUserNotFoundException::new);

    ChatRoom chatRoom = chatRoomRepository.findByIdAndDeletedAtIsNull(roomId)
        .orElseThrow(SocketRoomNotFoundException::new);

    if (!chatRoom.isParticipatedUser(user)) {
      throw new SocketNoAuthorizedException();
    }

    chatRoom.updateLastMessage(message.getMessage(), message.getMessageType(),
        LocalDateTime.now());

    ChatMessage chatMessage = ChatMessage.of(chatRoom,
        user,
        message.getMessage(),
        message.getMessageType());

    chatMessageRepository.save(chatMessage);

    eventPublisher.publishEvent(ChatMessageSendEvent.from(chatMessage));

    return MessageDto.fromEntity(chatMessage);
  }

  /**
   * 채팅 목록 조회
   *
   * @param roomId
   * @return
   */
  @Transactional(readOnly = true)
  public Slice<MessageDto> getChatList(Long roomId, Pageable pageable) {

    log.info("[getChatList] 채팅방 채팅 목록 조회 - 채팅방 : {}", roomId);

    ChatRoom chatRoom = chatRoomRepository.findByIdAndDeletedAtIsNull(roomId)
        .orElseThrow(() -> new SocketException(ROOM_NOT_FOUND));

    return chatMessageRepository.findAllByRoom(chatRoom, pageable)
        .map(MessageDto::fromEntity);
  }

}
