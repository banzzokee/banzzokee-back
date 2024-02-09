package homes.banzzokee.domain.user.service;

import static homes.banzzokee.domain.type.FilePath.PROFILE;
import static homes.banzzokee.domain.type.Role.ROLE_SHELTER;
import static homes.banzzokee.domain.type.Role.ROLE_USER;
import static homes.banzzokee.event.type.FcmTopicAction.SUBSCRIBE;
import static homes.banzzokee.event.type.FcmTopicAction.UNSUBSCRIBE;
import static homes.banzzokee.global.util.MockDataUtil.createMockMultipartFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.FollowRepository;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.PasswordChangeRequest;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.UserProfileUpdateRequest;
import homes.banzzokee.domain.user.dto.UserWithdrawRequest;
import homes.banzzokee.domain.user.entity.Follow;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.CanFollowOnlyShelterUserException;
import homes.banzzokee.domain.user.exception.CanNotFollowSelfException;
import homes.banzzokee.domain.user.exception.OriginPasswordEqualsNewPasswordException;
import homes.banzzokee.domain.user.exception.UserAlreadyWithdrawnException;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.event.FcmTopicStatusChangeEvent;
import homes.banzzokee.event.dto.FcmTopicStatusDto;
import homes.banzzokee.event.type.FcmTopicCategory;
import homes.banzzokee.global.error.exception.CustomException;
import homes.banzzokee.infra.fileupload.dto.FileDto;
import homes.banzzokee.infra.fileupload.service.FileUploadService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private FileUploadService s3Service;

  @Mock
  private UserRepository userRepository;

  @Mock
  private FollowRepository followRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  private final static MultipartFile MOCK_FILE;

  static {
    try {
      MOCK_FILE = createMockMultipartFile("profileImg",
          "src/test/resources/images/banzzokee.png");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final static LocalDateTime NOW = LocalDateTime.now();

  private final static UserWithdrawRequest USER_WITHDRAW_REQUEST
      = UserWithdrawRequest.builder()
      .password("1q2W#e$R")
      .build();

  private final static UserProfileUpdateRequest USER_PROFILE_UPDATE_REQUEST
      = UserProfileUpdateRequest.builder()
      .nickname("nickname")
      .introduce("introduce")
      .build();

  @Test
  @DisplayName("[사용자 프로필 조회] - 사용자를 찾을 수 없으면 UserNotFoundException 발생")
  void getUserProfile_when_userNotExists_then_throwUserNotFoundException() {
    // given
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThrows(UserNotFoundException.class,
        () -> userService.getUserProfile(0L));
  }

  @Test
  @DisplayName("[사용자 프로필 조회] - 승인된 보호소 포함")
  void getUserProfile_when_shelterIsVerified_then_includeShelter() {
    // given
    Shelter shelter = createMockShelter();
    given(shelter.isVerified()).willReturn(true);

    User user = createMockUser();
    given(user.getShelter()).willReturn(shelter);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when
    UserProfileDto profile = userService.getUserProfile(user.getId());

    // then
    assertEquals(user.getId(), profile.getUserId());
    assertEquals(user.getEmail(), profile.getEmail());
    assertEquals(user.getProfileImageUrl(), profile.getProfileImgUrl());
    assertEquals(user.getNickname(), profile.getNickname());
    assertEquals(user.getIntroduce(), profile.getIntroduce());
    assertEquals(user.getCreatedAt().toLocalDate(), NOW.toLocalDate());
    assertEquals(shelter.getId(), profile.getShelter().getShelterId());
    assertEquals(shelter.getName(), profile.getShelter().getName());
    assertEquals(shelter.getDescription(), profile.getShelter().getDescription());
    assertEquals(shelter.getTel(), profile.getShelter().getTel());
    assertEquals(shelter.getAddress(), profile.getShelter().getAddress());
    assertEquals(shelter.getCreatedAt().toLocalDate(), NOW.toLocalDate());
  }

  @Test
  @DisplayName("[사용자 프로필 조회] - 승인되지 않은 보호소는 미포함")
  void getUserProfile_when_shelterIsNotVerified_then_shelterIsNull() {
    // given
    Shelter shelter = createMockShelter();
    User user = createMockUser();
    given(user.getShelter()).willReturn(shelter);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when
    UserProfileDto profile = userService.getUserProfile(user.getId());

    // then
    assertEquals(user.getId(), profile.getUserId());
    assertEquals(user.getEmail(), profile.getEmail());
    assertEquals(user.getProfileImageUrl(), profile.getProfileImgUrl());
    assertEquals(user.getNickname(), profile.getNickname());
    assertEquals(user.getIntroduce(), profile.getIntroduce());
    assertEquals(user.getCreatedAt().toLocalDate(), NOW.toLocalDate());
    assertNull(profile.getShelter());
  }

  @Test
  @DisplayName("[사용자 탈퇴] - 사용자를 찾을 수 없으면 UserNotFoundException 발생")
  void withdrawUser_when_userNotExists_then_throwUserNotFoundException() {
    // given
    given(userRepository.findById(anyLong())).willReturn(Optional.empty());

    // when & then
    assertThrows(UserNotFoundException.class,
        () -> userService.withdrawUser(USER_WITHDRAW_REQUEST, 0L));
  }

  @Test
  @DisplayName("[사용자 탈퇴] - 성공 시 isWithdrawn true 반환")
  void withdrawUser_when_success_then_isWithdrawnIsTrue() {
    // given
    User user = createMockUser();
    given(user.getPassword()).willReturn(USER_WITHDRAW_REQUEST.getPassword());
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when
    userService.withdrawUser(USER_WITHDRAW_REQUEST, user.getId());

    // then
    assertTrue(user.isWithdrawn());
    verify(user).withdraw();
  }

  @Test
  @DisplayName("[사용자 탈퇴] - 이미 탈퇴한 사용자는 UserAlreadyWithdrawnException 발생")
  void withdrawUser_when_userAlreadyWithdrawn_then_throwUserAlreadyWithdrawnException() {
    // given
    User user = createMockUser();
    given(user.isWithdrawn()).willReturn(true);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when & then
    assertThrows(UserAlreadyWithdrawnException.class,
        () -> userService.withdrawUser(USER_WITHDRAW_REQUEST, user.getId()));
    verify(user, never()).withdraw();
  }

  @Test
  @DisplayName("[사용자 탈퇴] - 입력한 패스워드가 다를 경우 PasswordUnmatchedException 발생")
  void withdrawUser_when_passwordUnmatched_then_throwThrowPasswordUnmatchedException() {
    // given
    User user = createMockUser();
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when & then
    assertThrows(CustomException.class,
        () -> userService.withdrawUser(USER_WITHDRAW_REQUEST, user.getId()));
  }

  @Test
  @DisplayName("[사용자 패스워드 변경] - 새로운 패스워드가 기존 패스워드와 같은 경우 OriginPasswordEqualsNewPasswordException 발생")
  void changePassword_when_originalPasswordEqualsNewPassword_then_throwOriginPasswordEqualsNewPasswordException() {
    // given
    User user = createMockUser();
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when & then
    PasswordChangeRequest request = PasswordChangeRequest.builder()
        .originPassword(user.getPassword())
        .newPassword(user.getPassword())
        .confirmPassword(user.getPassword())
        .build();

    assertThrows(OriginPasswordEqualsNewPasswordException.class,
        () -> userService.changePassword(request, user.getId()));
  }

  @Test
  @DisplayName("[사용자 패스워드 변경] - 사용자의 패스워드와 입력한 패스워드가 다른 경우 PasswordUnmatchedException 발생")
  void changePassword_when_userPasswordNotEqualsUserInputPassword_then_throwPasswordUnmatchedException() {
    // given
    User user = createMockUser();
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when & then
    PasswordChangeRequest request = PasswordChangeRequest.builder()
        .originPassword(user.getPassword() + "123")
        .build();

    // TODO: CustomException -> PasswordUnmatchedException
    assertThrows(CustomException.class,
        () -> userService.changePassword(request, user.getId()));
  }

  @Test
  @DisplayName("[사용자 패스워드 변경] - 새로운 패스워드와 재입력 패스워드가 다른 경우 ConfirmPasswordUnmatchedException 발생")
  void changePassword_when_newPasswordNotEqualsConfirmPassword_then_throwConfirmPasswordUnmatchedException() {
    // given
    User user = createMockUser();
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when & then
    PasswordChangeRequest request = PasswordChangeRequest.builder()
        .originPassword(user.getPassword())
        .newPassword("54321")
        .confirmPassword("12345")
        .build();

    // TODO: CustomException -> ConfirmPasswordUnmatchedException
    assertThrows(CustomException.class,
        () -> userService.changePassword(request, user.getId()));
  }

  @Test
  @DisplayName("[사용자 패스워드 변경] - 성공 검증")
  void changePassword_when_success_then_verify() {
    // given
    User user = createMockUser();
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when
    String newPassword = user.getPassword() + "1234";
    PasswordChangeRequest request = PasswordChangeRequest.builder()
        .originPassword(user.getPassword())
        .newPassword(newPassword)
        .confirmPassword(newPassword)
        .build();
    userService.changePassword(request, user.getId());

    // then
    verify(user).changePassword(newPassword);
    assertEquals(newPassword, user.getPassword());
  }

  @Test
  @DisplayName("[사용자 팔로우] - 스스로 팔로우하면 CanNotFollowSelfException 발생")
  void followUser_when_followerEqualsFollowee_then_throwCanNotFollowSelfException() {
    // given
    User user = createMockUser();

    // when & then
    assertThrows(CanNotFollowSelfException.class,
        () -> userService.followUser(user.getId(), user.getId()));
  }

  @Test
  @DisplayName("[사용자 팔로우] - SHELTER 권한이 없는 사용자를 팔로우하면 CanFollowOnlyShelterUserException 발생")
  void followUser_when_UserDontHaveShelterRole_then_throwCanFollowOnlyShelterUserException() {
    // given
    User followee = createMockUser();
    followee.addRoles(ROLE_USER);
    given(userRepository.findById(followee.getId())).willReturn(Optional.of(followee));

    // when & then
    assertThrows(CanFollowOnlyShelterUserException.class,
        () -> userService.followUser(followee.getId(), anyLong()));
  }

  @Test
  @DisplayName("[사용자 팔로우] - 성공 검증, 팔로우가 처음인 경우")
  void followUser_when_success_then_verify() {
    // given
    User followee = createMockUser();
    followee.addRoles(ROLE_SHELTER);
    Shelter shelter = createMockShelter();
    given(followee.getShelter()).willReturn(shelter);
    given(userRepository.findById(followee.getId())).willReturn(Optional.of(followee));

    User follower = createMockUser();
    given(follower.getId()).willReturn(2L);
    given(userRepository.findById(follower.getId())).willReturn(Optional.of(follower));

    given(followRepository.findByFolloweeIdAndFollowerId(followee.getId(),
        follower.getId())).willReturn(Optional.empty());

    given(followRepository.save(any(Follow.class)))
        .willReturn(Follow.builder()
            .followee(followee)
            .follower(follower)
            .build());

    // when
    FollowDto follow = userService.followUser(followee.getId(), follower.getId());

    // then
    assertEquals(follower.getId(), follow.getFollower().getUserId());
    assertEquals(follower.getNickname(), follow.getFollower().getNickname());
    assertEquals(followee.getId(), follow.getFollowee().getUserId());
    assertEquals(followee.getNickname(), follow.getFollowee().getNickname());

    ArgumentCaptor<FcmTopicStatusChangeEvent> eventCaptor = ArgumentCaptor.forClass(
        FcmTopicStatusChangeEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    FcmTopicStatusDto eventPayload = eventCaptor.getValue().getPayload();
    assertEquals(SUBSCRIBE, eventPayload.getAction());
    assertEquals(shelter.getId(), eventPayload.getTopicId());
    assertEquals("topic." + FcmTopicCategory.SHELTER.getName() + "." + shelter.getId(),
        eventPayload.getTopic());
    assertEquals(follower.getId(), eventPayload.getUserId());
  }

  @Test
  @DisplayName("[사용자 팔로우] - 성공 검증, 팔로우가 처음인 아닌 경우")
  void followUser_when_alreadyFollow_then_neverSave() {
    // given
    User followee = createMockUser();
    followee.addRoles(ROLE_SHELTER);
    given(userRepository.findById(followee.getId())).willReturn(Optional.of(followee));

    User follower = createMockUser();
    given(follower.getId()).willReturn(2L);
    given(userRepository.findById(follower.getId())).willReturn(Optional.of(follower));

    given(followRepository.findByFolloweeIdAndFollowerId(followee.getId(),
        follower.getId())).willReturn(Optional.of(Follow.builder()
        .followee(followee)
        .follower(follower)
        .build()));

    // when
    FollowDto follow = userService.followUser(followee.getId(), follower.getId());

    // then
    verify(followRepository, never()).save(any(Follow.class));
    assertEquals(follower.getId(), follow.getFollower().getUserId());
    assertEquals(follower.getNickname(), follow.getFollower().getNickname());
    assertEquals(followee.getId(), follow.getFollowee().getUserId());
    assertEquals(followee.getNickname(), follow.getFollowee().getNickname());

    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  @DisplayName("[사용자 언팔로우] - 팔로우 정보가 없으면 delete, publishEvent 호출하지 않음")
  void unfollowUser_when_followNotExists_then_verifyNever_delete_and_publishEvent() {
    // given
    given(followRepository.findByFolloweeIdAndFollowerId(anyLong(), anyLong()))
        .willReturn(Optional.empty());

    // when & then
    userService.unfollowUser(1L, 2L);

    verify(followRepository, never()).delete(any(Follow.class));
    verify(eventPublisher, never()).publishEvent(any(FcmTopicStatusChangeEvent.class));
  }

  @Test
  @DisplayName("[사용자 언팔로우] - 팔로우 정보가 있으면 delete, publishEvent 호출")
  void unfollowUser_when_followExists_then_verify_delete_and_publishEvent() {
    // given
    User followee = createMockUser();
    User follower = createMockUser();
    given(follower.getId()).willReturn(2L);

    Shelter shelter = createMockShelter();
    given(followee.getShelter()).willReturn(shelter);

    Follow follow = Follow.builder()
        .followee(followee)
        .follower(follower)
        .build();

    given(followRepository.findByFolloweeIdAndFollowerId(followee.getId(),
        follower.getId())).willReturn(Optional.of(follow));

    // when & then
    userService.unfollowUser(1L, 2L);

    ArgumentCaptor<Follow> followCaptor = ArgumentCaptor.forClass(Follow.class);
    verify(followRepository).delete(followCaptor.capture());
    assertEquals(1L, followCaptor.getValue().getFollowee().getId());
    assertEquals(2L, followCaptor.getValue().getFollower().getId());

    ArgumentCaptor<FcmTopicStatusChangeEvent> eventCaptor = ArgumentCaptor.forClass(
        FcmTopicStatusChangeEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    FcmTopicStatusDto eventPayload = eventCaptor.getValue().getPayload();
    assertEquals(UNSUBSCRIBE, eventPayload.getAction());
    assertEquals(shelter.getId(), eventPayload.getTopicId());
    assertEquals(
        "topic." + eventPayload.getTopicCategory().getName() + "."
            + eventPayload.getTopicId(),
        eventPayload.getTopic());
    assertEquals(follower.getId(), eventPayload.getUserId());
  }

  @Test
  @DisplayName("[사용자 프로필 수정] - 파일이 있으면 업로드 호출 검증")
  void updateUserProfile_when_profileFileIsNotNull_then_verifyUploadOneFile() {
    // given
    User user = createMockUser();
    given(user.getProfileImage()).willReturn(null);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    given(s3Service.uploadOneFile(any(MultipartFile.class), any(FilePath.class)))
        .willReturn(FileDto.builder()
            .url("url")
            .build());

    // when
    userService.updateUserProfile(USER_PROFILE_UPDATE_REQUEST, MOCK_FILE, user.getId());

    // then
    verify(s3Service).uploadOneFile(MOCK_FILE, PROFILE);
  }

  @Test
  @DisplayName("[사용자 프로필 수정] - 파일이 없으면 업로드 호출 검증")
  void updateUserProfile_when_profileFileIsNull_then_verifyNeverUploadOneFile() {
    // given
    User user = createMockUser();
    given(user.getProfileImage()).willReturn(null);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when
    userService.updateUserProfile(USER_PROFILE_UPDATE_REQUEST, null, user.getId());

    // then
    verify(s3Service, never()).uploadOneFile(any(), any());
  }

  @Test
  @DisplayName("[사용자 프로필 수정] - 원본 프로필 사진이 없으면 삭제 호출 검증")
  void updateUserProfile_when_originProfileIsNull_then_verifyNeverDeleteFile() {
    // given
    User user = createMockUser();
    given(user.getProfileImage()).willReturn(null);
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when
    userService.updateUserProfile(USER_PROFILE_UPDATE_REQUEST, MOCK_FILE, user.getId());

    // then
    verify(s3Service, never()).deleteFile(anyString());
  }

  @Test
  @DisplayName("[사용자 프로필 수정] - 원본 프로필 사진이 있으면 삭제 호출 검증")
  void updateUserProfile_when_originProfileIsNotNull_then_verifyDeleteFile() {
    // given
    FileDto file = FileDto.builder().url("url").filename("filename").build();

    User user = createMockUser();
    given(user.getProfileImage()).willReturn(S3Object.from(file));
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when
    userService.updateUserProfile(USER_PROFILE_UPDATE_REQUEST, null, user.getId());

    // then
    verify(s3Service).deleteFile(file.getUrl());
  }

  private static Shelter createMockShelter() {
    Shelter shelter = spy(Shelter.builder()
        .name("보호소1")
        .description("보호소1 설명")
        .tel("010-1234-5678")
        .address("서울시 행복구")
        .user(User.builder().build())
        .build());
    lenient().when(shelter.getId()).thenReturn(1L);
    lenient().when(shelter.getCreatedAt()).thenReturn(NOW);
    return shelter;
  }

  private static User createMockUser() {
    User user = spy(User.builder()
        .email("email")
        .password("password")
        .profileImgUrl("profileImgUrl")
        .nickname("nickname")
        .introduce("introduce")
        .role(new HashSet<>())
        .build());
    lenient().when(user.getId()).thenReturn(1L);
    lenient().when(user.getCreatedAt()).thenReturn(NOW);
    return user;
  }
}