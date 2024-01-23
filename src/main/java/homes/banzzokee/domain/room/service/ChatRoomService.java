package homes.banzzokee.domain.room.service;

import static homes.banzzokee.global.error.ErrorCode.FAILED;

import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.dto.CreateRoomResponse;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.error.exception.CustomException;
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

  private final ChatRoomRepository chatRoomRepository;
  private final UserRepository userRepository;
  private final ShelterRepository shelterRepository;
//  private final AdoptionRepository;

  public CreateRoomResponse createChatRoom(String email, Long adoptionId,
      Long userId) {

    // todo: userRepository 에 findByUidAndDeletedAtNull(String email) 추가
    // todo: FAILED -> USER_NOT_FOUND
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(FAILED));

    // todo: 아래 코드 지우고 Adoption 에서 받아온 shelter 로 저장
    Shelter shelter = shelterRepository.findById(1L)
        .orElseThrow(() -> new CustomException(FAILED));

    return CreateRoomResponse.fromEntity(
        chatRoomRepository.save(ChatRoom.builder()
            .user(user)
            .shelter(shelter)
            .build()
        )
    );

  }

}
