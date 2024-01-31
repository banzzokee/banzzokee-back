package homes.banzzokee.domain.room.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.chat.dto.LastChatMessageDto;
import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.dto.ChatRoomDto;
import homes.banzzokee.domain.room.dto.RoomCreateResponse;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
   * @param userId
   * @return
   */
  public RoomCreateResponse createChatRoom(String email, Long adoptionId,
      Long userId) {

    // todo: userRepository 에 findByUidAndDeletedAtNull(String email) 추가
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    Adoption adoption = adoptionRepository.findById(adoptionId)
        .orElseThrow(AdoptionNotFoundException::new);

    // 게시글 등록한 shelter
    Shelter shelter = adoption.getUser().getShelter();

    // todo: 이미 있는 채팅방 (userId, shelterId, adoptionId) 중복 validation 체크

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
  public List<ChatRoomDto> getChatRooms(String email) {
    User user = userRepository.findByEmailAndDeletedAtNull(email)
        .orElseThrow(UserNotFoundException::new);

    // 반환할 채팅방 목록
    List<ChatRoomDto> chatRoomDtoList = new ArrayList<>();
    // 채팅방 목록 추출
    List<ChatRoom> chatRooms = chatRoomRepository.findAllByUser(user);
    LastChatMessageDto lastChatMessage;

    for (ChatRoom chatRoom : chatRooms) {
      // 채팅방 별 마지막 채팅 정보 추출
      lastChatMessage = chatRoomRepository.findLastChatMessageByRoom(chatRoom)
          .orElse(null);

      // 추출된 채팅이 null 이 아니면
      if (lastChatMessage != null) {
        // 반환될 채팅방 목록에 주입
        chatRoomDtoList.add(ChatRoomDto.fromEntity(chatRoom, lastChatMessage));
      }
    }

    // 마지막 채팅이 올라온 시간으로 내림차순 정렬 후 반환
    return chatRoomDtoList.stream()
        .sorted(Comparator.comparing(ChatRoomDto::getLastMessageCreatedAt).reversed())
        .collect(Collectors.toList());
  }

}
