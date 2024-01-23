package homes.banzzokee.domain.user.service;

import static homes.banzzokee.domain.type.LoginType.EMAIL;
import static homes.banzzokee.domain.type.Role.SHELTER;
import static homes.banzzokee.domain.type.Role.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.type.Role;
import homes.banzzokee.domain.user.dao.FollowRepository;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.CanFollowOnlyShelterUserException;
import homes.banzzokee.domain.user.exception.CanNotFollowSelfException;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.config.jpa.JpaAuditingConfig;
import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(JpaAuditingConfig.class)
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

  private UserService userService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FollowRepository followRepository;

  @Autowired
  private ShelterRepository shelterRepository;

  private User user1;

  private User user2;

  @PostConstruct
  private void initialize() {
    userService = new UserService(userRepository, followRepository);
  }

  @BeforeAll
  public void setup() {
    Set<Role> roles1 = new HashSet<>();
    roles1.add(USER);

    Set<Role> roles2 = new HashSet<>();
    roles2.add(SHELTER);

    Shelter shelter1 = shelterRepository.save(Shelter.builder()
        .name("보호소1")
        .verified(true)
        .build());

    Shelter shelter2 = shelterRepository.save(Shelter.builder()
        .name("보호소2")
        .verified(false)
        .build());

    user1 = userRepository.save(User.builder()
        .email("user1@banzzokee.homes")
        .nickname("사용자1")
        .profileImgUrl("avatar.png")
        .introduce("안녕하세요.")
        .loginType(EMAIL)
        .role(roles1)
        .shelter(shelter1)
        .build());

    user2 = userRepository.save(User.builder()
        .email("user2@banzzokee.homes")
        .nickname("사용자2")
        .profileImgUrl("avatar.png")
        .introduce("안녕하세요.")
        .loginType(EMAIL)
        .role(roles2)
        .shelter(shelter2)
        .build());
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
    assertNotNull(userProfile.shelter());
  }

  @Test
  @DisplayName("승인되지 않은 보호소는 사용자 프로필 조회시 null로 반환")
  void getUserProfile_Shelter_Is_Null_When_Shelter_Is_Not_Verified() {
    // given
    // when
    UserProfileDto userProfile = userService.getUserProfile(user2.getId());

    // then
    assertNull(userProfile.shelter());
  }

  @Test
  @DisplayName("사용자 본인이 팔로우하면 CanNotFollowSelfException 발생")
  void followUser_Throw_CanNotFollowSelfException_When_Follower_Same_Followee() {
    // given
    // when
    // then
    assertThrows(CanNotFollowSelfException.class,
        () -> userService.followUser(1L, 1L));
  }

  @Test
  @DisplayName("팔로우하는 사용자가 Shelter 권한이 없으면 CanFollowOnlyShelterUserException 발생")
  void followUser_Throw_CanFollowOnlyShelterUserException_When_User_Dont_Has_Role_Shelter() {
    // given
    // when
    // then
    assertThrows(CanFollowOnlyShelterUserException.class,
        () -> userService.followUser(2L, 1L));
  }

  @Test
  @DisplayName("사용자 팔로우 성공")
  void success_FollowUser() {
    // given
    // when
    FollowDto follow = userService.followUser(2L, 1L);

    // then
    assertEquals(1L, follow.follower().userId());
    assertEquals("사용자1", follow.follower().nickname());
    assertEquals(2L, follow.followee().userId());
    assertEquals("사용자2", follow.followee().nickname());
  }
}