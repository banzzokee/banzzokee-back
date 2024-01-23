package homes.banzzokee.domain.user.service;

import static homes.banzzokee.domain.type.LoginType.EMAIL;
import static homes.banzzokee.domain.type.Role.USER;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import homes.banzzokee.domain.shelter.dao.ShelterRepository;
import homes.banzzokee.domain.shelter.entity.Shelter;
import homes.banzzokee.domain.type.Role;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.entity.User;
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
  private ShelterRepository shelterRepository;

  @PostConstruct
  private void initialize() {
    userService = new UserService(userRepository);
  }

  @BeforeAll
  public void setup() {
    Set<Role> roles = new HashSet<>();
    roles.add(USER);

    Shelter shelter1 = shelterRepository.save(Shelter.builder()
        .name("보호소1")
        .verified(true)
        .build());

    Shelter shelter2 = shelterRepository.save(Shelter.builder()
        .name("보호소2")
        .verified(false)
        .build());

    userRepository.save(User.builder()
        .email("user1@banzzokee.homes")
        .nickname("사용자1")
        .profileImgUrl("avatar.png")
        .introduce("안녕하세요.")
        .loginType(EMAIL)
        .role(roles)
        .shelter(shelter1)
        .build());

    userRepository.save(User.builder()
        .email("user2@banzzokee.homes")
        .nickname("사용자2")
        .profileImgUrl("avatar.png")
        .introduce("안녕하세요.")
        .loginType(EMAIL)
        .role(roles)
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
    UserProfileDto userProfile = userService.getUserProfile(1L);

    // then
    assertNotNull(userProfile.shelter());
  }

  @Test
  @DisplayName("승인되지 않은 보호소는 사용자 프로필 조회시 null로 반환")
  void getUserProfile_Shelter_Is_Null_When_Shelter_Is_Not_Verified() {
    // given
    // when
    UserProfileDto userProfile = userService.getUserProfile(2L);

    // then
    assertNull(userProfile.shelter());
  }
}