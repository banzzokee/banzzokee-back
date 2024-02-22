package homes.banzzokee.domain.room.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.chat.dao.ChatMessageRepository;
import homes.banzzokee.domain.chat.dto.MessageDto;
import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.dto.ChatRoomDto;
import homes.banzzokee.domain.room.dto.ChatUserDto;
import homes.banzzokee.domain.room.dto.RoomCreateResponse;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.room.exception.AdoptionWriterException;
import homes.banzzokee.domain.room.exception.AlreadyExistsChatRoomException;
import homes.banzzokee.domain.room.exception.RoomNotFoundException;
import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.type.MessageType;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 채팅방 service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

  private final ShelterRepository shelterRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final AdoptionRepository adoptionRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;

  private final SimpMessagingTemplate template;

  /**
   * 채팅방 생성
   *
   * @param email
   * @param adoptionId
   * @return
   */
  public RoomCreateResponse createChatRoom(String email, Long adoptionId) {

    // 삭제되지 않은 유저
    User user = userRepository.findByEmailAndDeletedAtNull(email)
        .orElseThrow(UserNotFoundException::new);

    Adoption adoption = adoptionRepository.findById(adoptionId)
        .orElseThrow(AdoptionNotFoundException::new);

    if (adoption.getUser().equals(user)) {
      throw new AdoptionWriterException();
    }

    Shelter shelter = adoption.getUser().getShelter();

    if (chatRoomRepository.existsByUserAndAdoption(user, adoption)) {
      throw new AlreadyExistsChatRoomException();
    }

    return RoomCreateResponse.fromEntity(
        chatRoomRepository.save(ChatRoom.builder()
            .user(user)
            .shelter(shelter)
            .adoption(adoption)
            .build()
        )
    );

  }

  /**
   * 채팅방 목록 조회
   *
   * @param email
   * @return
   */
  public Slice<ChatRoomDto> getChatRooms(String email, Pageable pageable) {
    User user = userRepository.findByEmailAndDeletedAtNull(email)
        .orElseThrow(UserNotFoundException::new);

    Shelter shelter = shelterRepository.getByUser(user);

    Slice<ChatRoom> chatRooms = chatRoomRepository
        .findAllByUserOrShelterOrderByLastMessageCreatedAtDesc(user, shelter, pageable);

    return new SliceImpl<>(
        chatRooms.stream()
            .map(ChatRoomDto::fromEntity)
            .collect(Collectors.toList())
    );
  }

  /**
   * 채팅방 퇴장
   *
   * @param email
   * @param roomId
   */
  @Transactional
  public void exitChatRoom(String email, Long roomId) {
    // email 로 유저 받아오기
    User user = userRepository.findByEmailAndDeletedAtNull(email)
        .orElseThrow(UserNotFoundException::new);

    ChatRoom chatRoom = chatRoomRepository.findById(roomId)
        .orElseThrow(RoomNotFoundException::new);

    // 본인이 속한 채팅방이 아닐 때
    if ((chatRoom.getUser() == null || !chatRoom.getUser().equals(user))
        && (chatRoom.getShelter() == null || !chatRoom.getShelter().getUser()
        .equals(user))) {
      throw new NoAuthorizedException();
    }

    // 해당 방에서 유저 삭제
    if (chatRoom.getShelter() != null && user.equals(
        chatRoom.getShelter().getUser())) { // 쉘터의 유저일때 Shelter 삭제
      log.info("[exitChatRoom] 보호소 퇴장");
      chatRoom.leaveShelter();
    } else if (user.equals(chatRoom.getUser())) {  // 그냥 유저일때 User 삭제
      log.info("[exitChatRoom] 일반 유저 퇴장");
      chatRoom.leaveUser();
    }

    MessageDto message = MessageDto.builder()
        .user(ChatUserDto.fromEntity(user))
        .message(user.getNickname() + "님이 퇴장하셨습니다.")
        .messageType(MessageType.EXIT)
        .createdAt(LocalDateTime.now())
        .build();

    chatMessageRepository.save(ChatMessage.builder()
        .room(chatRoom)
        .user(user)
        .message(message.getMessage())
        .messageType(message.getMessageType())
        .build()
    );

    // template.convertToSend 로 메세지 보내기 확인
    template.convertAndSend("/topic/chats.rooms." + roomId, message);
  }
}
