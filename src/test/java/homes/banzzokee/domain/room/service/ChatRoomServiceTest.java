package homes.banzzokee.domain.room.service;

import static homes.banzzokee.global.error.ErrorCode.FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.config.jpa.JpaAuditingConfig;
import homes.banzzokee.global.config.querydsl.QueryDSLConfig;
import homes.banzzokee.global.error.exception.CustomException;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({JpaAuditingConfig.class, QueryDSLConfig.class})
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatRoomServiceTest {

  private ChatRoomService chatRoomService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ShelterRepository shelterRepository;

  @Autowired
  private ChatRoomRepository chatRoomRepository;

  private User user;
  private Shelter shelter;

  @PostConstruct
  private void initialize() {
    chatRoomService = new ChatRoomService(chatRoomRepository, userRepository,
        shelterRepository);
  }

  @BeforeAll
  public void setup() {
    user = userRepository.save(User.builder()
        .nickname("test_nickname")
        .build());

    User shelterUser = userRepository.save(User.builder().build());
    shelter = shelterRepository.save(Shelter.builder()
        .name("test_shelter_name")
        .user(shelterUser)
        .build());
  }

  @Test
  @DisplayName("[service] 채팅방 생성 성공")
  void success_createChatRoom() {
    //given
    //when
    ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder()
        .user(user)
        .shelter(shelter)
        .build());

    //then
    assertNotNull(chatRoom);
  }

  @Test
  @DisplayName("[service] 채팅방 생성 실패 - USER_NOT_FOUND")
  void failed_createChatRoom_USER_NOT_FOUND() {
    //given
    //when
    CustomException exception = assertThrows(CustomException.class,
        () -> chatRoomService.createChatRoom("test", 1L, 0L));

    // todo: FAILED -> USER_NOT_FOUND
    //then
    assertEquals(FAILED, exception.getErrorCode());
  }


}