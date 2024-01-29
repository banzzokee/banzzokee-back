package homes.banzzokee.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import homes.banzzokee.domain.shelter.dto.ShelterDto;
import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.FollowDto.FollowUserDto;
import homes.banzzokee.domain.user.dto.PasswordChangeRequest;
import homes.banzzokee.domain.user.dto.PasswordChangeResponse;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.UserProfileUpdateRequest;
import homes.banzzokee.domain.user.dto.UserProfileUpdateResponse;
import homes.banzzokee.domain.user.dto.UserWithdrawRequest;
import homes.banzzokee.domain.user.dto.UserWithdrawResponse;
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
import org.springframework.web.multipart.MultipartFile;

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
    given(userService.getUserProfile(1L)).willReturn(
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
    UserWithdrawRequest request = UserWithdrawRequest.builder().password("1q2W#e$R")
        .build();

    given(userService.withdrawUser(any(UserWithdrawRequest.class), anyLong()))
        .willReturn(UserWithdrawResponse.builder()
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
    PasswordChangeRequest request = PasswordChangeRequest.builder()
        .originPassword("1q2W#e$R")
        .newPassword("1q2W#e$R1")
        .confirmPassword("1q2W#e$R1")
        .build();

    given(userService.changePassword(any(PasswordChangeRequest.class), anyLong()))
        .willReturn(PasswordChangeResponse.builder()
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
    given(userService.followUser(2L, 1L))
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
    UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
        .nickname("nickname")
        .introduce("introduce")
        .build();
    MockPart mockPart = MockDataUtil.createMockPart("request", request);
    MockMultipartFile mockFile = MockDataUtil.createMockMultipartFile("profileImg",
        "src/test/resources/images/banzzokee.png");

    MockMultipartHttpServletRequestBuilder patch = MockMvcRequestBuilders
        .multipart(PATCH, "/api/users/me?userId=1")
        .file(mockFile)
        .part(mockPart);

    given(userService.updateUserProfile(eq(request), any(MultipartFile.class), eq(1L)))
        .willReturn(UserProfileUpdateResponse.builder()
            .userId(1L)
            .email("email")
            .profileImgUrl("profileImgUrl")
            .nickname(request.getNickname())
            .introduce(request.getIntroduce())
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