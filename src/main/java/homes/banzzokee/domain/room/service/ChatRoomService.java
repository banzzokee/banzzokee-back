package homes.banzzokee.domain.room.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.dto.ChatRoomDto;
import homes.banzzokee.domain.room.dto.RoomCreateResponse;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.room.exception.AdoptionWriterException;
import homes.banzzokee.domain.room.exception.AlreadyExistsChatRoomException;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

/**
 * 채팅방 service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

  private final AdoptionRepository adoptionRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;

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

    Slice<ChatRoom> chatRooms = chatRoomRepository
        .findAllByUserOrderByLastMessageCreatedAtDesc(user, pageable);

    return new SliceImpl<>(
        chatRooms.stream()
            .map(ChatRoomDto::fromEntity)
            .collect(Collectors.toList())
    );
  }

}
