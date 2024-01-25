package homes.banzzokee.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import homes.banzzokee.domain.shelter.dto.ShelterDto;
import homes.banzzokee.domain.user.dto.ChangePasswordRequest;
import homes.banzzokee.domain.user.dto.ChangePasswordResponse;
import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.FollowDto.FollowUserDto;
import homes.banzzokee.domain.user.dto.UpdateUserRequest;
import homes.banzzokee.domain.user.dto.UpdateUserResponse;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.WithdrawUserRequest;
import homes.banzzokee.domain.user.dto.WithdrawUserResponse;
import homes.banzzokee.domain.user.service.UserService;
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.global.util.MockMvcUtil;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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

  @Test
  @DisplayName("사용자 패스워드 변경 성공")
  void successChangePassword() throws Exception {
    // given
    ChangePasswordRequest request = ChangePasswordRequest.builder()
        .originPassword("1q2W#e$R")
        .newPassword("1q2W#e$R1")
        .confirmPassword("1q2W#e$R1")
        .build();

    given(userService.changePassword(request, 1))
        .willReturn(ChangePasswordResponse.builder()
            .userId(1L)
            .email("user1@banzzokee.homes")
            .build());

    // when
    ResultActions resultActions = MockMvcUtil.performPatch(mockMvc,
        "/api/users/me/change-password?userId=1", request);

    // then
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.email").value("user1@banzzokee.homes"));
  }

  @Test
  @DisplayName("사용자 팔로우 성공")
  void successFollowUser() throws Exception {
    // given
    given(userService.followUser(anyLong(), anyLong()))
        .willReturn(FollowDto.builder()
            .follower(FollowUserDto.builder()
                .userId(1L)
                .nickname("사용자1")
                .build())
            .followee(FollowUserDto.builder()
                .userId(2L)
                .nickname("사용자2")
                .build())
            .build());

    // when
    ResultActions resultActions = MockMvcUtil
        .performPost(mockMvc, "/api/users/2/follow?followerId=1", null);

    // then
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.follower.userId").value(1))
        .andExpect(jsonPath("$.follower.nickname").value("사용자1"))
        .andExpect(jsonPath("$.followee.userId").value(2))
        .andExpect(jsonPath("$.followee.nickname").value("사용자2"));
  }

  @Test
  @DisplayName("사용자 언팔로우 성공")
  void successUnfollowUser() throws Exception {
    // given
    // when
    ResultActions resultActions = MockMvcUtil
        .performPost(mockMvc, "/api/users/2/unfollow?followerId=1", null);

    // then
    resultActions.andExpect(status().isOk());
  }

  @Test
  void successUpdateUserProfile() throws Exception {
    // given
    ObjectMapper objectMapper = new ObjectMapper();

    UpdateUserRequest request = UpdateUserRequest.builder()
        .nickname("nickname")
        .introduce("introduce")
        .build();

    MockPart mockPart = new MockPart("request", objectMapper.writeValueAsBytes(request));
    mockPart.getHeaders().set("Content-Type", "application/json");

    MockMultipartFile mockFile = MockDataUtil.createMockMultipartFile(
        "src/test/resources/images/banzzokee.png");

    MockMultipartHttpServletRequestBuilder patch = MockMvcRequestBuilders
        .multipart(PATCH, "/api/users/me?userId=1")
        .file("profileImg", mockFile.getBytes())
        .part(mockPart);

    given(userService.updateUserProfile(any(), any(), anyLong()))
        .willReturn(UpdateUserResponse.builder()
            .userId(1L)
            .email("email")
            .profileImgUrl("profileImgUrl")
            .nickname(request.nickname())
            .introduce(request.introduce())
            .build());

    // when
    ResultActions resultActions = mockMvc.perform(patch).andDo(print());

    // then
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.email").value("email"))
        .andExpect(jsonPath("$.profileImgUrl").value("profileImgUrl"))
        .andExpect(jsonPath("$.nickname").value("nickname"))
        .andExpect(jsonPath("$.introduce").value("introduce"));
  }
}