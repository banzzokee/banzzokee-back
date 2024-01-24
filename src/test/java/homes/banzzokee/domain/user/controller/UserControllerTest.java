package homes.banzzokee.domain.user.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import homes.banzzokee.domain.shelter.dto.ShelterDto;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.WithdrawUserRequest;
import homes.banzzokee.domain.user.dto.WithdrawUserResponse;
import homes.banzzokee.domain.user.service.UserService;
import homes.banzzokee.global.util.MockMvcUtil;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(value = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  @DisplayName("사용자 프로필 조회 성공")
  void successGetUserProfile() throws Exception {
    // given
    given(userService.getUserProfile(anyLong())).willReturn(
        UserProfileDto.builder()
            .userId(1L)
            .email("user1@banzzokee.homes")
            .profileImgUrl("avatar.png")
            .nickname("사용자1")
            .introduce("안녕하세요")
            .joinedAt(LocalDate.of(2024, 1, 1))
            .shelter(ShelterDto.builder()
                .shelterId(1L)
                .shelterImgUrl("shelter.png")
                .name("반쪽이 보호소")
                .description("반쪽이 화이팅")
                .tel("02-1234-5678")
                .address("서울시 행복구")
                .registeredAt(LocalDate.of(2024, 1, 1))
                .build())
            .build());

    // when
    ResultActions resultActions = MockMvcUtil.performGet(mockMvc, "/api/users/1");

    // then
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.email").value("user1@banzzokee.homes"))
        .andExpect(jsonPath("$.profileImgUrl").value("avatar.png"))
        .andExpect(jsonPath("$.nickname").value("사용자1"))
        .andExpect(jsonPath("$.introduce").value("안녕하세요"))
        .andExpect(jsonPath("$.joinedAt").value("2024-01-01"))
        .andExpect(jsonPath("$.shelter.shelterId").value(1))
        .andExpect(jsonPath("$.shelter.shelterImgUrl").value("shelter.png"))
        .andExpect(jsonPath("$.shelter.name").value("반쪽이 보호소"))
        .andExpect(jsonPath("$.shelter.description").value("반쪽이 화이팅"))
        .andExpect(jsonPath("$.shelter.tel").value("02-1234-5678"))
        .andExpect(jsonPath("$.shelter.address").value("서울시 행복구"))
        .andExpect(jsonPath("$.shelter.registeredAt").value("2024-01-01"));
  }

  @Test
  @DisplayName("사용자 탈퇴 성공")
  void successWithdrawUser() throws Exception {
    // given
    WithdrawUserRequest request = new WithdrawUserRequest("1q2W#e$R");

    given(userService.withdrawUser(request, 1))
        .willReturn(WithdrawUserResponse.builder()
            .userId(1L)
            .email("user1@banzzokee.homes")
            .build());

    // when
    ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
        "/api/users/me/withdraw?userId=1", request);

    // then
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.email").value("user1@banzzokee.homes"));
  }
}