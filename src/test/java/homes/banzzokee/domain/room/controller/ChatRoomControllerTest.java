package homes.banzzokee.domain.room.controller;

import static homes.banzzokee.domain.type.MessageType.TEXT;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import homes.banzzokee.domain.room.dto.ChatAdoptionDto;
import homes.banzzokee.domain.room.dto.ChatRoomDto;
import homes.banzzokee.domain.room.dto.ChatUserDto;
import homes.banzzokee.domain.room.service.ChatRoomService;
import homes.banzzokee.global.security.jwt.JwtAuthenticationFilter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatRoomControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ChatRoomService chatRoomService;

  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

//  @Test
//  @DisplayName("[controller] 채팅방 생성 성공")
//  void success_createChatRoom() throws Exception {
//    //given
//    given(chatRoomService.createChatRoom(anyString(), anyLong(), anyLong()))
//        .willReturn(RoomCreateResponse.builder()
//            .roomId(1L)
//            .user(ChatUserDto.builder()
//                .userId(1L)
//                .nickname("test_nickname")
//                .build())
//            .shelter(ChatShelterDto.builder()
//                .shelterId(1L)
//                .name("test_shelter_name")
//                .build())
//            .build());
//
//    //when
//    ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
//        "/api/rooms/adoptions/1", null);
//
//    //then
//    resultActions.andExpect(status().isOk())
//        .andExpect(jsonPath("$.roomId").value(1L))
//        .andExpect(jsonPath("$.user.userId").value(1L))
//        .andExpect(jsonPath("$.user.nickname").value("test_nickname"))
//        .andExpect(jsonPath("$.shelter.shelterId").value(1L))
//        .andExpect(jsonPath("$.shelter.name").value("test_shelter_name"));
//
//  }

  @Test
  @WithMockUser
  @DisplayName("[채팅방 목록 조회] - 성공 검증")
  void getChatRooms_when_validInput_then_success() throws Exception {
    //given
    ChatUserDto chatUserDto = ChatUserDto.builder()
        .userId(1L)
        .nickname("테스트_유저_1")
        .build();

    ChatAdoptionDto chatAdoptionDto1 = ChatAdoptionDto.builder()
        .adoptionId(1L)
        .title("테스트_입양글_제목_1")
        .content("테스트_입양글_설명글_1")
        .firstImgUrl("테스트_입양글_첫번째_사진_1")
        .build();
    ChatAdoptionDto chatAdoptionDto2 = ChatAdoptionDto.builder()
        .adoptionId(2L)
        .title("테스트_입양글_제목_2")
        .content("테스트_입양글_설명글_2")
        .firstImgUrl("테스트_입양글_첫번째_사진_2")
        .build();
    ChatAdoptionDto chatAdoptionDto3 = ChatAdoptionDto.builder()
        .adoptionId(3L)
        .title("테스트_입양글_제목_3")
        .content("테스트_입양글_설명글_3")
        .firstImgUrl("테스트_입양글_첫번째_사진_3")
        .build();
    ChatAdoptionDto chatAdoptionDto4 = ChatAdoptionDto.builder()
        .adoptionId(4L)
        .title("테스트_입양글_제목_4")
        .content("테스트_입양글_설명글_4")
        .firstImgUrl("테스트_입양글_첫번째_사진_4")
        .build();

    PageRequest pageRequest = PageRequest.of(0, 3);
    List<ChatRoomDto> chatRoomDtoList = new ArrayList<>();

    ChatRoomDto dto1 = ChatRoomDto.builder()
        .roomId(1L)
        .user(chatUserDto)
        .adoption(chatAdoptionDto1)
        .lastMessage("1번_채팅방_마지막채팅")
        .lastMessageType(TEXT)
        .lastMessageCreatedAt(LocalDateTime.now().minusDays(4L))
        .build();
    ChatRoomDto dto2 = ChatRoomDto.builder()
        .roomId(2L)
        .user(chatUserDto)
        .adoption(chatAdoptionDto2)
        .lastMessage("2번_채팅방_마지막채팅")
        .lastMessageType(TEXT)
        .lastMessageCreatedAt(LocalDateTime.now().minusDays(1L))
        .build();
    ChatRoomDto dto3 = ChatRoomDto.builder()
        .roomId(3L)
        .user(chatUserDto)
        .adoption(chatAdoptionDto3)
        .lastMessage("3번_채팅방_마지막채팅")
        .lastMessageType(TEXT)
        .lastMessageCreatedAt(LocalDateTime.now().minusDays(2L))
        .build();
    ChatRoomDto dto4 = ChatRoomDto.builder()
        .roomId(4L)
        .user(chatUserDto)
        .adoption(chatAdoptionDto4)
        .lastMessage("4번_채팅방_마지막채팅")
        .lastMessageType(TEXT)
        .lastMessageCreatedAt(LocalDateTime.now().minusDays(3L))
        .build();

    chatRoomDtoList.add(dto1);
    chatRoomDtoList.add(dto2);
    chatRoomDtoList.add(dto3);
    chatRoomDtoList.add(dto4);

    given(chatRoomService.getChatRooms(anyString(), eq(pageRequest)))
        .willReturn(new SliceImpl<>(chatRoomDtoList));

    //when
    //then
    mockMvc.perform(get("/api/rooms?page=0&size=3"))

        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.content[0].roomId").value(1L))
        .andExpect(jsonPath("$.content[0].adoption.adoptionId").value(1L))
        .andExpect(jsonPath("$.content[0].lastMessage").value("1번_채팅방_마지막채팅"))
    ;

  }

}