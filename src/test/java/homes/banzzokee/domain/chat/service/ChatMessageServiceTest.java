package homes.banzzokee.domain.chat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import homes.banzzokee.domain.chat.dao.ChatMessageRepository;
import homes.banzzokee.domain.chat.dto.ChatSendDto;
import homes.banzzokee.domain.chat.dto.MessageDto;
import homes.banzzokee.domain.chat.entity.ChatMessage;
import homes.banzzokee.domain.room.dao.ChatRoomRepository;
import homes.banzzokee.domain.room.entity.ChatRoom;
import homes.banzzokee.domain.room.exception.SocketRoomNotFoundException;
import homes.banzzokee.domain.room.exception.SocketUserNotFoundException;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.type.MessageType;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.event.ChatMessageSendEvent;
import homes.banzzokee.event.dto.ChatMessagePayload;
import homes.banzzokee.global.config.stomp.exception.SocketNoAuthorizedException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

  @InjectMocks
  private ChatMessageService chatMessageService;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChatMessageRepository chatMessageRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Test
  @DisplayName("[채팅 메세지 전송 성공] - 기대값 일치")
  void sendMessage_when_success_assertEqualsAll() {
    //given
    User user = mock(User.class);
    given(user.getId()).willReturn(1L);

    Shelter shelter = mock(Shelter.class);
    User shelterUser = mock(User.class);
    given(shelter.getUser()).willReturn(shelterUser);
    given(shelterUser.getId()).willReturn(2L);

    ChatRoom chatRoom = ChatRoom.builder()
        .shelter(shelter)
        .user(user)
        .build();

    given(chatRoomRepository.findByIdAndDeletedAtIsNull(eq(1L)))
        .willReturn(Optional.of(chatRoom));

    given(userRepository.findByEmailAndDeletedAtNull(eq("IncludedUser")))
        .willReturn(Optional.of(user));

    ChatSendDto chatSendDto = ChatSendDto.builder()
        .message("첫 채팅 메세지입니다.")
        .messageType(MessageType.TEXT)
        .build();

    given(chatMessageRepository.save(any(ChatMessage.class)))
        .willReturn(ChatMessage.builder()
            .room(chatRoom)
            .user(user)
            .message(chatSendDto.getMessage())
            .messageType(chatSendDto.getMessageType())
            .build()
        );

    //when
    MessageDto response = chatMessageService.sendMessage("IncludedUser", 1L,
        chatSendDto);

    //then
    assertEquals(response.getMessage(), chatSendDto.getMessage());
    assertEquals(response.getUser().getUserId(), 1L);
    assertEquals(chatRoom.getLastMessage(), chatSendDto.getMessage());

    ArgumentCaptor<ChatMessageSendEvent> eventCaptor =
        ArgumentCaptor.forClass(ChatMessageSendEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    ChatMessagePayload payload = eventCaptor.getValue().getPayload();
    assertEquals(payload.getMessage(), chatSendDto.getMessage());
    assertEquals(payload.getMessageType(), chatSendDto.getMessageType());
    assertEquals(payload.getSenderId(), 1L);
    assertEquals(payload.getReceiverId(), 2L);
  }

  @Test
  @DisplayName("[채팅 메세지 전송 실패] - 존재하지 않는 유저")
  void sendMessage_when_userNotFound_then_throwSocketUserNotFoundException() {
    //given
    given(userRepository.findByEmailAndDeletedAtNull(eq("test")))
        .willReturn(Optional.empty());

    //when
    //then
    assertThrows(SocketUserNotFoundException.class,
        () -> chatMessageService.sendMessage("test", 1L, null));
  }

  @Test
  @DisplayName("[채팅 메세지 전송 실패] - 존재하지 않는 채팅방")
  void sendMessage_when_roomNotFound_then_throwSocketRoomNotFoundException() {
    //given
    given(userRepository.findByEmailAndDeletedAtNull(eq("test")))
        .willReturn(Optional.of(
                User.builder()
                    .email("test")
                    .build()
            )
        );

    given(chatRoomRepository.findByIdAndDeletedAtIsNull(eq(1L)))
        .willReturn(Optional.empty());

    //when
    //then
    assertThrows(SocketRoomNotFoundException.class,
        () -> chatMessageService.sendMessage("test", 1L, null));
  }

  @Test
  @DisplayName("[채팅 메세지 전송 실패] - 본인이 속한 채팅방이 아님")
  void sendMessage_when_notIncludeChatRoom_then_throwNoAuthorizedException() {
    //given
    User user = mock(User.class);
    User shelterUser = mock(User.class);
    Shelter shelter = mock(Shelter.class);
    given(shelter.getUser()).willReturn(shelterUser);

    given(userRepository.findByEmailAndDeletedAtNull(eq("wrongUser")))
        .willReturn(Optional.of(
                User.builder()
                    .email("wrongUser")
                    .build()
            )
        );

    given(chatRoomRepository.findByIdAndDeletedAtIsNull(eq(1L)))
        .willReturn(Optional.of(
                ChatRoom.builder()
                    .user(user)
                    .shelter(shelter)
                    .build()
            )
        );

    //when
    //then
    assertThrows(SocketNoAuthorizedException.class,
        () -> chatMessageService.sendMessage("wrongUser", 1L, null));
  }


}