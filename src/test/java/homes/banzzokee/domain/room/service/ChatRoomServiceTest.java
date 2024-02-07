package homes.banzzokee.domain.room.service;

import static homes.banzzokee.domain.type.Role.ROLE_SHELTER;
import static homes.banzzokee.domain.type.Role.ROLE_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.chat.dao.ChatMessageRepository;
import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.dto.ChatRoomDto;
import homes.banzzokee.domain.room.dto.ChatUserDto;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.room.exception.AdoptionWriterException;
import homes.banzzokee.domain.room.exception.AlreadyExistsChatRoomException;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.type.AdoptionStatus;
import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.domain.type.LoginType;
import homes.banzzokee.domain.type.MessageType;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

  @InjectMocks
  private ChatRoomService chatRoomService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AdoptionRepository adoptionRepository;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatMessageRepository chatMessageRepository;

  @Mock
  private SimpMessagingTemplate template;

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
  private final static User SHELTER_USER = User.builder()
      .email(SHELTER_USER_EMAIL)
      .nickname("ShelterUser")
      .role(Set.of(ROLE_USER, ROLE_SHELTER))
      .introduce("I'm user had shelter")
      .profileImgUrl("It's shelter user's profileImgUrl")
      .loginType(LoginType.EMAIL)
      .shelter(null)
      .build();
  private final static Adoption FIRST_ADOPTION = Adoption.builder()
      .title("firstAdoptionTitle")
      .content("firstAdoptionContent")
      .status(AdoptionStatus.ADOPTING)
      .breed(BreedType.ALASKAN_MALAMUTE)
      .size(DogSize.LARGE)
      .neutering(true)
      .gender(DogGender.MALE)
      .age(5)
      .healthChecked(true)
      .registeredAt(null)
      .user(SHELTER_USER)
      .build();


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


  @Test
  @DisplayName("[채팅방 생성 성공] - 성공 시 데이터 일치")
  void createChatRoom_when_success_then_assertEquals_all() {
    //given
    Adoption adoption = mock(Adoption.class);
    Shelter shelter = mock(Shelter.class);
    User user = mock(User.class);
    User otherUser = mock(User.class);
    given(user.getId()).willReturn(1L);
    given(user.getNickname()).willReturn("GeneralUser");

    given(adoption.getUser()).willReturn(otherUser);
    given(otherUser.getShelter()).willReturn(shelter);
    given(shelter.getId()).willReturn(1L);

    given(userRepository.findByEmailAndDeletedAtNull(eq(GENERAL_USER_EMAIL)))
        .willReturn(Optional.of(GENERAL_USER));

    given(adoptionRepository.findById(1L))
        .willReturn(Optional.of(adoption));

    given(
        chatRoomRepository.existsByUserAndAdoption(any(User.class), any(Adoption.class)))
        .willReturn(false);

    ChatRoom chatRoom = ChatRoom.builder()
        .adoption(adoption)
        .shelter(shelter)
        .user(GENERAL_USER)
        .build();

    given(chatRoomRepository.save(any(ChatRoom.class)))
        .willReturn(chatRoom);

    ChatUserDto chatUserDto = ChatUserDto.fromEntity(user);

    //when
    chatRoomService.createChatRoom(GENERAL_USER_EMAIL, 1L);
    ArgumentCaptor<ChatRoom> captor = ArgumentCaptor.forClass(ChatRoom.class);

    //then
    verify(chatRoomRepository, times(1)).save(captor.capture());
    ChatRoom savedChatRoom = captor.getValue();

    assertEquals(chatUserDto.getNickname(), savedChatRoom.getUser().getNickname());

  }

  @Test
  @DisplayName("[채팅방 생성 실패] - 사용자 찾을 수 없음")
  void createChatRoom_when_userNotFound_then_throwUserNotFoundException() {
    //given
    given(userRepository.findByEmailAndDeletedAtNull(eq(GENERAL_USER_EMAIL)))
        .willReturn(Optional.empty());

    //when
    //then
    assertThrows(UserNotFoundException.class,
        () -> chatRoomService.createChatRoom(GENERAL_USER_EMAIL, 1L)
    );
  }

  @Test
  @DisplayName("[채팅방 생성 실패] - 입양글 찾을 수 없음")
  void createChatRoom_when_adoptionNotFound_then_throwAdoptionNotFoundException() {
    //given
    given(userRepository.findByEmailAndDeletedAtNull(eq(GENERAL_USER_EMAIL)))
        .willReturn(Optional.of(User.builder()
            .email(GENERAL_USER_EMAIL)
            .build()));
    given(adoptionRepository.findById(eq(1L)))
        .willReturn(Optional.empty());

    //when
    //then
    assertThrows(AdoptionNotFoundException.class,
        () -> chatRoomService.createChatRoom(GENERAL_USER_EMAIL, 1L)
    );
  }

  @Test
  @DisplayName("[채팅방 생성 실패] - 입양글 작성 유저와 같은 유저")
  void createChatRoom_when_equalUser_then_throwAdoptionWriterException() {
    //given
    given(userRepository.findByEmailAndDeletedAtNull(eq(GENERAL_USER_EMAIL)))
        .willReturn(Optional.of(GENERAL_USER));
    given(adoptionRepository.findById(eq(1L)))
        .willReturn(Optional.of(
            Adoption.builder()
                .user(GENERAL_USER)
                .build()
        ));

    //when
    //then
    assertThrows(AdoptionWriterException.class,
        () -> chatRoomService.createChatRoom(GENERAL_USER_EMAIL, 1L)
    );
  }

  @Test
  @DisplayName("[채팅방 생성 실패] - 이미 만들어진 채팅방")
  void createChatRoom_when_alreadyExists_then_throwAlreadyExistsChatRoomException() {
    //given
    given(userRepository.findByEmailAndDeletedAtNull(eq(GENERAL_USER_EMAIL)))
        .willReturn(Optional.of(GENERAL_USER));

    Adoption adoption = Adoption.builder()
        .user(User.builder()
            .email("otherUser")
            .build())
        .build();
    given(adoptionRepository.findById(eq(1L)))
        .willReturn(Optional.of(adoption));

    given(chatRoomRepository.existsByUserAndAdoption(eq(GENERAL_USER), eq(adoption)))
        .willReturn(true);

    //when
    //then
    assertThrows(AlreadyExistsChatRoomException.class,
        () -> chatRoomService.createChatRoom(GENERAL_USER_EMAIL, 1L)
    );
  }

  @Test
  @DisplayName("[채팅방 나가기 성공] - 기대값 일치")
  void exitChatRoom_when_success_assertEqualsAll() {
    //given
    User shelterUser = mock(User.class);
    Shelter shelter = mock(Shelter.class);

    given(shelter.getUser()).willReturn(shelterUser);

    given(userRepository.findByEmailAndDeletedAtNull(eq(GENERAL_USER_EMAIL)))
        .willReturn(Optional.of(GENERAL_USER));

    ChatRoom chatRoom = ChatRoom.builder()
        .user(GENERAL_USER)
        .shelter(shelter)
        .build();

    given(chatRoomRepository.findById(eq(1L)))
        .willReturn(Optional.of(chatRoom));

    given(chatMessageRepository.save(any(ChatMessage.class)))
        .willReturn(ChatMessage.builder()
            .room(chatRoom)
            .user(GENERAL_USER)
            .message("퇴장하였습니다.")
            .messageType(MessageType.EXIT)
            .build());

    //when
    chatRoomService.exitChatRoom(GENERAL_USER_EMAIL, 1L);

    //then
    assertNull(chatRoom.getUser());
  }

  @Test
  @DisplayName("[채팅방 나가기 실패] - 본인이 속한방이 아님")
  void exitChatRoom_when_notIncludeChatRoom_then_throwNoAuthorizedException() {
    //given
    User otherUser = mock(User.class);
    User shelterUser = mock(User.class);
    Shelter shelter = mock(Shelter.class);

    given(shelter.getUser()).willReturn(shelterUser);

    given(userRepository.findByEmailAndDeletedAtNull(eq("otherUserEmail")))
        .willReturn(Optional.of(otherUser));

    ChatRoom chatRoom = ChatRoom.builder()
        .user(GENERAL_USER)
        .shelter(shelter)
        .build();

    given(chatRoomRepository.findById(eq(1L)))
        .willReturn(Optional.of(chatRoom));

    //when
    //then
    assertThrows(NoAuthorizedException.class,
        () -> chatRoomService.exitChatRoom("otherUserEmail", 1L));
  }


}