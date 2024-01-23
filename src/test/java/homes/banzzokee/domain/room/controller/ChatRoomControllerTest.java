package homes.banzzokee.domain.room.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import homes.banzzokee.domain.room.dto.ChatShelterDto;
import homes.banzzokee.domain.room.dto.ChatUserDto;
import homes.banzzokee.domain.room.dto.CreateRoomResponse;
import homes.banzzokee.domain.room.service.ChatRoomService;
import homes.banzzokee.global.util.MockMvcUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(value = ChatRoomController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ChatRoomControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ChatRoomService chatRoomService;

  @Test
  @DisplayName("[controller] 채팅방 생성 성공")
  void success_createChatRoom() throws Exception {
    //given
    given(chatRoomService.createChatRoom(anyString(), anyLong(), anyLong()))
        .willReturn(CreateRoomResponse.builder()
            .roomId(1L)
            .user(ChatUserDto.builder()
                .userId(1L)
                .nickname("test_nickname")
                .build())
            .shelter(ChatShelterDto.builder()
                .shelterId(1L)
                .name("test_shelter_name")
                .build())
            .build());

    //when
    ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
        "/api/rooms/adoptions/1", null);

    //then
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.roomId").value(1L))
        .andExpect(jsonPath("$.user.userId").value(1L))
        .andExpect(jsonPath("$.user.nickname").value("test_nickname"))
        .andExpect(jsonPath("$.shelter.shelterId").value(1L))
        .andExpect(jsonPath("$.shelter.name").value("test_shelter_name"));

  }

}