package homes.banzzokee.domain.user.service;

import static homes.banzzokee.domain.type.LoginType.EMAIL;
import static homes.banzzokee.domain.type.Role.SHELTER;
import static homes.banzzokee.domain.type.Role.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.type.Role;
import homes.banzzokee.domain.user.dao.FollowRepository;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.dto.ChangePasswordRequest;
import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.UserProfileUpdateRequest;
import homes.banzzokee.domain.user.dto.WithdrawUserRequest;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.CanFollowOnlyShelterUserException;
import homes.banzzokee.domain.user.exception.CanNotFollowSelfException;
import homes.banzzokee.domain.user.exception.OriginPasswordEqualsNewPasswordException;
import homes.banzzokee.domain.user.exception.UserAlreadyWithdrawnException;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.config.jpa.JpaAuditingConfig;
import homes.banzzokee.global.error.exception.CustomException;
import homes.banzzokee.global.util.MockDataUtil;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartFile;

@Import(JpaAuditingConfig.class)
@DataJpaTest
class UserServiceTest {

  private UserService userService;

  private FileUploadService s3Service;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FollowRepository followRepository;

  @Autowired
  private ShelterRepository shelterRepository;

  private User user1;
  private User user2;
  private User user3;
  private MultipartFile mockFile;

  private final static WithdrawUserRequest withdrawUserRequest = new WithdrawUserRequest(
      "1q2W#e$R");

  @PostConstruct
  private void initialize() throws IOException {
    s3Service = Mockito.mock(FileUploadService.class);
    userService = new UserService(userRepository, followRepository, s3Service);
    mockFile = MockDataUtil.createMockMultipartFile(
        "src/test/resources/images/banzzokee.png");
  }

  @BeforeEach
  public void setup() {
    Set<Role> roles1 = new HashSet<>();
    roles1.add(USER);

    Set<Role> roles2 = new HashSet<>();
    roles2.add(SHELTER);

    user1 = userRepository.save(User.builder()
        .email("user1@banzzokee.homes")
        .password("1q2W#e$R")
        .nickname("사용자1")
        .introduce("안녕하세요.")
        .loginType(EMAIL)
        .role(roles1)
        .build());

    user2 = userRepository.save(User.builder()
        .email("user2@banzzokee.homes")
        .nickname("사용자2")
        .profileImgUrl("avatar.png")
        .introduce("안녕하세요.")
        .loginType(EMAIL)
        .role(roles2)
        .build());

    user3 = userRepository.save(User.builder()
        .email("user3@banzzokee.homes")
        .password("1q2W#e$R")
        .build());

    Shelter shelter1 = shelterRepository.save(Shelter.builder()
        .name("보호소1")
        .verified(true)
        .user(user1)
        .build());

    user1.registerShelter(shelter1);

    Shelter shelter2 = shelterRepository.save(Shelter.builder()
        .name("보호소2")
        .verified(false)
        .user(user2)
        .build());

    user2.registerShelter(shelter2);
  }

  @Test
  @DisplayName("사용자가 없을 때 UserNotFoundException 발생")
  void getUserProfile_Throw_UserNotFoundException_When_User_Not_Exists() {
    // given
    // when
    // then
    assertThrows(UserNotFoundException.class,
        () -> userService.getUserProfile(0L));
  }

  @Test
  @DisplayName("승인된 보호소는 사용자 프로필 조회시 null이면 안됨")
  void getUserProfile_Shelter_Is_Not_Null_When_Shelter_Is_Verified() {
    // given
    // when
    UserProfileDto userProfile = userService.getUserProfile(user1.getId());

    // then
    assertNotNull(userProfile.getShelter());
  }

  @Test
  @DisplayName("승인되지 않은 보호소는 사용자 프로필 조회시 null로 반환")
  void getUserProfile_Shelter_Is_Null_When_Shelter_Is_Not_Verified() {
    // given
    // when
    UserProfileDto userProfile = userService.getUserProfile(user2.getId());

    // then
    assertNull(userProfile.getShelter());
  }

  @Test
  @DisplayName("[회원탈퇴] 사용자가 없을 때 UserNotFoundException 발생")
  void withdrawUser_Throw_UserNotFoundException_When_User_Not_Exists() {
    // given
    // when
    // then
    assertThrows(UserNotFoundException.class,
        () -> userService.withdrawUser(withdrawUserRequest, 0L));
  }

  @Test
  @DisplayName("[회원탈퇴] 성공 시 user.isWithdrawn true 반환")
  void withdrawUser_isWithdrawn_Is_True_When_Success() {
    // given
    // when
    user3.withdraw();
    userRepository.save(user3);
    user3 = userRepository.findById(user3.getId()).get();

    // then
    assertTrue(user3.isWithdrawn());
  }

  @Test
  @DisplayName("[회원탈퇴] 이미 탈퇴한 사용자 UserAlreadyWithdrawnException 발생")
  void withdrawUser_Throw_UserAlreadyWithdrawnException_When_User_Already_Withdrawn() {
    // given
    // when
    user3.withdraw();
    userRepository.save(user3);

    // then
    assertThrows(UserAlreadyWithdrawnException.class,
        () -> userService.withdrawUser(withdrawUserRequest, user3.getId()));
  }

  @Test
  @DisplayName("[회원탈퇴] 패스워드가 다를 경우 PasswordUnmatchedException 발생")
  void withdrawUser_Throw_PasswordUnmatchedException_When_Password_Unmatched() {
    // given
    WithdrawUserRequest request = new WithdrawUserRequest("1234");

    // when
    // then
    assertThrows(CustomException.class,
        () -> userService.withdrawUser(request, user3.getId()));
  }

  @Test
  @DisplayName("[패스워드 변경] 기존 패스워드와 같은 경우 OriginPasswordEqualsNewPasswordException 발생")
  void changePassword_Throw_OriginPasswordEqualsNewPasswordException_When_OriginPassword_Equals_NewPassword() {
    // given
    ChangePasswordRequest request = ChangePasswordRequest.builder()
        .originPassword(user1.getPassword())
        .newPassword(user1.getPassword())
        .confirmPassword(user1.getPassword())
        .build();

    // when
    // then
    assertThrows(OriginPasswordEqualsNewPasswordException.class,
        () -> userService.changePassword(request, user1.getId()));
  }

  @Test
  @DisplayName("[패스워드 변경] 사용자의 패스워드와 입력한 originPassword가 다른 경우 PasswordUnmatchedException 발생")
  void changePassword_Throw_PasswordUnmatchedException_When_UserPassword_Not_Equals_OriginPassword() {
    // given
    ChangePasswordRequest request = ChangePasswordRequest.builder()
        .originPassword(user1.getPassword() + "123")
        .build();

    // when
    // then
    assertThrows(CustomException.class,
        () -> userService.changePassword(request, user1.getId()));
  }

  @Test
  @DisplayName("[패스워드 변경] 새로운 패스워드와 재입력 패스워드가 다른 경우 ConfirmPasswordUnmatchedException 발생")
  void changePassword_Throw_ConfirmPasswordUnmatchedException_When_NewPassword_Not_Equals_ConfirmPassword() {
    // given
    ChangePasswordRequest request = ChangePasswordRequest.builder()
        .originPassword(user1.getPassword())
        .newPassword("1q2W#e$R1")
        .confirmPassword("1q2W#e$R2")
        .build();

    // when
    // then
    assertThrows(CustomException.class,
        () -> userService.changePassword(request, user1.getId()));
  }

  @Test
  @DisplayName("[패스워드 변경] 성공 시 로그인 성공")
  void changePassword_Success_SignIn_When_Success() {
    // given
    String newPassword = user1.getPassword() + "123";

    ChangePasswordRequest request = ChangePasswordRequest.builder()
        .originPassword(user1.getPassword())
        .newPassword(newPassword)
        .confirmPassword(newPassword)
        .build();

    // when
    // then
    userService.changePassword(request, user1.getId());
    // TODO 로그인 확인
  }

  @DisplayName("사용자 본인이 팔로우하면 CanNotFollowSelfException 발생")
  void followUser_Throw_CanNotFollowSelfException_When_Follower_Same_Followee() {
    // given
    // when
    // then
    assertThrows(CanNotFollowSelfException.class,
        () -> userService.followUser(user1.getId(), user1.getId()));
  }

  @Test
  @DisplayName("팔로우하는 사용자가 Shelter 권한이 없으면 CanFollowOnlyShelterUserException 발생")
  void followUser_Throw_CanFollowOnlyShelterUserException_When_User_Dont_Has_Role_Shelter() {
    // given
    // when
    // then
    assertThrows(CanFollowOnlyShelterUserException.class,
        () -> userService.followUser(user1.getId(), user2.getId()));
  }

  @Test
  @DisplayName("사용자 팔로우 성공")
  void success_FollowUser() {
    // given
    // when
    FollowDto follow = userService.followUser(user2.getId(), user1.getId());

    // then
    assertEquals(user1.getId(), follow.follower().userId());
    assertEquals(user1.getNickname(), follow.follower().nickname());
    assertEquals(user2.getId(), follow.followee().userId());
    assertEquals(user2.getNickname(), follow.followee().nickname());
  }

  @Test
  @DisplayName("[프로필 수정] 파일이 있으면 업로드 호출 검증")
  void updateUserProfile_Verify_uploadOneFile_1_Times_When_ProfileFile_Is_Not_Null()
      throws IOException {
    // given
    UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
        .nickname("nickname")
        .introduce("introduce")
        .build();

    // when
    userService.updateUserProfile(request, mockFile, user1.getId());

    // then
    verify(s3Service, times(1)).uploadOneFile(mockFile);
  }

  @Test
  @DisplayName("[프로필 수정] 파일이 없으면 업로드 호출 검증")
  void updateUserProfile_Verify_uploadOneFile_0_Times_When_ProfileFile_Is_Null()
      throws IOException {
    // given
    UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
        .nickname("nickname")
        .introduce("introduce")
        .build();

    // when
    userService.updateUserProfile(request, null, user1.getId());

    // then
    verify(s3Service, times(0)).uploadOneFile(any());
  }

  @Test
  @DisplayName("[프로필 수정] 원본 프로필 사진이 없으면 삭제 호출 검증")
  void updateUserProfile_Verify_deleteFile_Never_When_Origin_ProfileFile_Is_Null()
      throws IOException {
    // given
    UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
        .nickname("nickname")
        .introduce("introduce")
        .build();

    // when
    userService.updateUserProfile(request, mockFile, user1.getId());

    // then
    verify(s3Service, never()).deleteFile(anyString());
  }

  @Test
  @DisplayName("[프로필 수정] 원본 프로필 사진이 있으면 삭제 호출 검증")
  void updateUserProfile_Verify_deleteFile_1_Times_When_Origin_ProfileFile_Is_Not_Null() {
    // given
    UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
        .nickname("nickname")
        .introduce("introduce")
        .build();

    // when
    userService.updateUserProfile(request, null, user2.getId());

    // then
    verify(s3Service, times(1)).deleteFile(any());
  }
}