package homes.banzzokee.domain.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
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
import homes.banzzokee.global.security.WithMockCustomUser;
import homes.banzzokee.global.security.jwt.JwtAuthenticationFilter;
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.global.util.MockMvcUtil;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(value = UserController.class,
    excludeAutoConfiguration = SecurityAutoConfiguration.class,
    excludeFilters = {
        @ComponentScan.Filter(type = ASSIGNABLE_TYPE,
            classes = {JwtAuthenticationFilter.class})
    })
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcUtil.build(new UserController(userService));
  }

  @Test
  @DisplayName("[사용자 프로필 조회] - 성공 검증")
  void getUserProfile_when_validInput_then_success() throws Exception {
    // given
    ShelterDto shelter = ShelterDto.builder()
        .shelterId(1L)
        .shelterImgUrl("shelter.png")
        .name("반쪽이 보호소")
        .description("반쪽이 화이팅")
        .tel("02-1234-5678")
        .address("서울시 행복구")
        .registeredAt(LocalDate.of(2024, 1, 1))
        .build();

    UserProfileDto profile = UserProfileDto.builder()
        .userId(1L)
        .email("user1@banzzokee.homes")
        .profileImgUrl("avatar.png")
        .nickname("사용자1")
        .introduce("안녕하세요")
        .joinedAt(LocalDate.of(2024, 1, 1))
        .shelter(shelter)
        .build();

    given(userService.getUserProfile(profile.getUserId()))
        .willReturn(profile);

    // when
    ResultActions resultActions = MockMvcUtil.performGet(mockMvc, "/api/users/1");

    // then
    verify(userService).getUserProfile(profile.getUserId());

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(profile.getUserId()))
        .andExpect(jsonPath("$.email").value(profile.getEmail()))
        .andExpect(jsonPath("$.profileImgUrl").value(profile.getProfileImgUrl()))
        .andExpect(jsonPath("$.nickname").value(profile.getNickname()))
        .andExpect(jsonPath("$.introduce").value(profile.getIntroduce()))
        .andExpect(jsonPath("$.joinedAt").value(profile.getJoinedAt().toString()))
        .andExpect(jsonPath("$.shelter.shelterId").value(shelter.getShelterId()))
        .andExpect(jsonPath("$.shelter.shelterImgUrl").value(shelter.getShelterImgUrl()))
        .andExpect(jsonPath("$.shelter.name").value(shelter.getName()))
        .andExpect(jsonPath("$.shelter.description").value(shelter.getDescription()))
        .andExpect(jsonPath("$.shelter.tel").value(shelter.getTel()))
        .andExpect(jsonPath("$.shelter.address").value(shelter.getAddress()))
        .andExpect(jsonPath("$.shelter.registeredAt")
            .value(shelter.getRegisteredAt().toString()));
  }

  @Test
  @DisplayName("[사용자 탈퇴] - 성공 검증")
  @WithMockCustomUser
  void withdrawUser_when_validInput_then_success() throws Exception {
    // given
    UserWithdrawRequest request =
        UserWithdrawRequest.builder()
            .password("1q2W#e$R")
            .build();

    UserWithdrawResponse response = UserWithdrawResponse.builder()
        .userId(1L)
        .email("user1@banzzokee.homes")
        .build();
    given(userService.withdrawUser(any(UserWithdrawRequest.class), anyLong()))
        .willReturn(response);

    // when
    ResultActions resultActions = MockMvcUtil.performPost(mockMvc,
        "/api/users/me/withdraw", request);

    // then
    verify(userService).withdrawUser(request, response.getUserId());

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(response.getUserId()))
        .andExpect(jsonPath("$.email").value(response.getEmail()));
  }

  @Test
  @DisplayName("[사용자 패스워드 변경] - 성공 검증")
  @WithMockCustomUser
  void changePassword_when_validInput_then_success() throws Exception {
    // given
    PasswordChangeRequest request = PasswordChangeRequest.builder()
        .originPassword("1q2W#e$R")
        .newPassword("1q2W#e$R1")
        .confirmPassword("1q2W#e$R1")
        .build();

    PasswordChangeResponse response = PasswordChangeResponse.builder()
        .userId(1L)
        .email("user1@banzzokee.homes")
        .build();
    given(userService.changePassword(any(PasswordChangeRequest.class), anyLong()))
        .willReturn(response);

    // when
    ResultActions resultActions = MockMvcUtil.performPatch(mockMvc,
        "/api/users/me/change-password", request);

    // then
    verify(userService).changePassword(request, response.getUserId());

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(response.getUserId()))
        .andExpect(jsonPath("$.email").value(response.getEmail()));
  }

  @Test
  @DisplayName("[사용자 팔로우] - 성공 검증")
  @WithMockCustomUser
  void followUser_when_validInput_then_success() throws Exception {
    // given
    FollowUserDto user1 = FollowUserDto.builder()
        .userId(1L)
        .nickname("사용자1")
        .build();
    FollowUserDto user2 = FollowUserDto.builder()
        .userId(2L)
        .nickname("사용자2")
        .build();
    given(userService.followUser(user2.getUserId(), user1.getUserId()))
        .willReturn(FollowDto.builder()
            .follower(user1)
            .followee(user2)
            .build());

    // when
    ResultActions resultActions = MockMvcUtil
        .performPost(mockMvc, "/api/users/2/follow", null);

    // then
    verify(userService).followUser(user2.getUserId(), user1.getUserId());

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.follower.userId").value(user1.getUserId()))
        .andExpect(jsonPath("$.follower.nickname").value(user1.getNickname()))
        .andExpect(jsonPath("$.followee.userId").value(user2.getUserId()))
        .andExpect(jsonPath("$.followee.nickname").value(user2.getNickname()));
  }

  @Test
  @DisplayName("[사용자 언팔로우] - 성공 검증")
  @WithMockCustomUser
  void unfollowUser_when_validInput_then_success() throws Exception {
    // when
    ResultActions resultActions = MockMvcUtil
        .performPost(mockMvc, "/api/users/2/unfollow", null);

    // then
    verify(userService).unfollowUser(2L, 1L);
    resultActions.andExpect(status().isOk());
  }

  @Test
  @DisplayName("[사용자 프로필 수정] - 성공 검증")
  @WithMockCustomUser
  void updateUserProfile_when_validInput_then_success() throws Exception {
    // given
    UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
        .nickname("nickname")
        .introduce("introduce")
        .build();
    MockPart mockPart = MockDataUtil.createMockPart("request", request);
    MockMultipartFile mockFile = MockDataUtil.createMockMultipartFile("profileImg",
        "src/test/resources/images/banzzokee.png");

    MockMultipartHttpServletRequestBuilder patch = MockMvcRequestBuilders
        .multipart(PATCH, "/api/users/me")
        .file(mockFile)
        .part(mockPart);

    UserProfileUpdateResponse response = UserProfileUpdateResponse.builder()
        .userId(1L)
        .email("email")
        .profileImgUrl("profileImgUrl")
        .nickname(request.getNickname())
        .introduce(request.getIntroduce())
        .build();
    given(userService.updateUserProfile(eq(request), any(MultipartFile.class), eq(1L)))
        .willReturn(response);

    // when
    ResultActions resultActions = mockMvc.perform(patch).andDo(print());

    // then
    verify(userService).updateUserProfile(request, mockFile, 1L);

    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(response.getUserId()))
        .andExpect(jsonPath("$.email").value(response.getEmail()))
        .andExpect(jsonPath("$.profileImgUrl").value(response.getProfileImgUrl()))
        .andExpect(jsonPath("$.nickname").value(response.getNickname()))
        .andExpect(jsonPath("$.introduce").value(response.getIntroduce()));
  }
}