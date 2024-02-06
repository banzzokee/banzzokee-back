package homes.banzzokee.domain.room.service;

import static homes.banzzokee.domain.type.Role.ROLE_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.dto.ChatRoomDto;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.type.LoginType;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

  @InjectMocks
  private ChatRoomService chatRoomService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  private static final String GENERAL_USER_EMAIL = "generalUserEmail";
  private static final String SHELTER_USER_EMAIL = "shelterUserEmail";
  private final static User GENERAL_USER = User.builder()
      .email(GENERAL_USER_EMAIL)
      .nickname("GeneralUser")
      .role(Set.of(ROLE_USER))
      .introduce("I'm general user")
      .profileImgUrl("It's general user's profileImgUrl")
      .loginType(LoginType.EMAIL)
      .shelter(null)
      .build();
//  private final static Shelter MOMO_SHELTER = Shelter.builder()
//      .name("MoMo")
//      .description("Welcome to MoMo Shelter")
//      .shelterImgUrl("It's momo shelter's imageUrl")
//      .tel("010-1234-1234")
//      .address("서울시 강남구")
//      .latitude(32.01)
//      .longitude(24.22)
//      .verified(true)
//      .build();
//  private final static User SHELTER_USER = User.builder()
//      .email(SHELTER_USER_EMAIL)
//      .nickname("ShelterUser")
//      .role(Set.of(ROLE_USER, ROLE_SHELTER))
//      .introduce("I'm user had shelter")
//      .profileImgUrl("It's shelter user's profileImgUrl")
//      .loginType(LoginType.EMAIL)
//      .shelter(MOMO_SHELTER)
//      .build();

  @Test
  @DisplayName("[채팅방 목록 조회 성공] - 성공 시 데이터 일치")
  void getChatRooms_when_success_then_assertEquals_all() {
    //given
    User user = User.builder()
        .email(GENERAL_USER_EMAIL)
        .nickname("GeneralUser")
        .role(Set.of(ROLE_USER))
        .introduce("I'm general user")
        .profileImgUrl("It's general user's profileImgUrl")
        .loginType(LoginType.EMAIL)
        .shelter(null)
        .build();

    Adoption adoption1 = Adoption.builder()
        .title("테스트_입양글_제목_1")
        .content("테스트_입양글_설명글_1")
        .build();
    Adoption adoption2 = Adoption.builder()
        .title("테스트_입양글_제목_2")
        .content("테스트_입양글_설명글_2")
        .build();
    Adoption adoption3 = Adoption.builder()
        .title("테스트_입양글_제목_3")
        .content("테스트_입양글_설명글_3")
        .build();
    Adoption adoption4 = Adoption.builder()
        .title("테스트_입양글_제목_4")
        .content("테스트_입양글_설명글_4")
        .build();

    PageRequest pageRequest = PageRequest.of(0, 3);
    List<ChatRoom> chatRoomList = new ArrayList<>();

    ChatRoom room1 = ChatRoom.builder()
        .user(GENERAL_USER)
        .adoption(adoption1)
        .build();
    ChatRoom room2 = ChatRoom.builder()
        .user(GENERAL_USER)
        .adoption(adoption2)
        .build();
    ChatRoom room3 = ChatRoom.builder()
        .user(GENERAL_USER)
        .adoption(adoption3)
        .build();
    ChatRoom room4 = ChatRoom.builder()
        .user(GENERAL_USER)
        .adoption(adoption4)
        .build();

    chatRoomList.add(room1);
    chatRoomList.add(room2);
    chatRoomList.add(room3);
    chatRoomList.add(room4);

    given(userRepository.findByEmailAndDeletedAtNull(anyString()))
        .willReturn(Optional.of(user));
    given(chatRoomRepository.findAllByUserOrderByLastMessageCreatedAtDesc(any(User.class),
        any(
            Pageable.class)))
        .willReturn(new PageImpl<>(chatRoomList, pageRequest, chatRoomList.size()));

    //when
    Slice<ChatRoomDto> chatRoomDtoSlice = chatRoomService.getChatRooms(
        GENERAL_USER_EMAIL, pageRequest
    );

    //then
    assertEquals(chatRoomDtoSlice.getContent().get(0).getUser().getNickname(),
        "GeneralUser");
  }

  @Test
  @DisplayName("[채팅방 목록 조회 실패] - 사용자 찾을 수 없음")
  void getChatRooms_when_userNotFound_then_throwUserNotFoundException() {
    //given
    given(userRepository.findByEmailAndDeletedAtNull(eq(GENERAL_USER_EMAIL)))
        .willReturn(Optional.empty());
    //when
    //then
    assertThrows(UserNotFoundException.class,
        () -> chatRoomService.getChatRooms(GENERAL_USER_EMAIL, null));
  }

//
//  @Test
//  @DisplayName("[service] 채팅방 생성 성공")
//  void success_createChatRoom() {
//    //given
//    //when
//    ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder()
//        .user(user)
//        .shelter(shelter)
//        .build());
//
//    //then
//    assertNotNull(chatRoom);
//  }
//
//  @Test
//  @DisplayName("[service] 채팅방 생성 실패 - USER_NOT_FOUND")
//  void failed_createChatRoom_USER_NOT_FOUND() {
//    //given
//    //when
//    CustomException exception = assertThrows(CustomException.class,
//        () -> chatRoomService.createChatRoom("test", 1L, 0L));
//
//    // todo: FAILED -> USER_NOT_FOUND
//    //then
//    assertEquals(FAILED, exception.getErrorCode());
//  }


}