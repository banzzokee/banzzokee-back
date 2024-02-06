package homes.banzzokee.domain.bookmark.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.bookmark.dao.BookmarkRepository;
import homes.banzzokee.domain.bookmark.entity.Bookmark;
import homes.banzzokee.domain.type.*;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static homes.banzzokee.global.util.MockDataUtil.createMockMultipartFile;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

  @InjectMocks
  private BookmarkService bookmarkService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AdoptionRepository adoptionRepository;

  @Mock
  private BookmarkRepository bookmarkRepository;

  private final static MultipartFile MOCK_FILE;

  static {
    try {
      MOCK_FILE = createMockMultipartFile("profileImg",
          "src/test/resources/images/banzzokee.png");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("[북마크 등록] - 성공 검증")
  void registerBookmark_when_success_then_verify() {
    // given
    long userId = 1L;
    long adoptionId = 1L;

    User user = User.builder()
        .email("test@gmail.com")
        .nickname("반쪽이")
        .role(Set.of(Role.ROLE_USER))
        .loginType(LoginType.EMAIL)
        .build();

    Adoption adoption = Adoption.builder()
        .title("강아지")
        .content("강아지 입니다.")
        .breed(BreedType.POODLE)
        .size(DogSize.SMALL)
        .neutering(true)
        .gender(DogGender.MALE)
        .age(1)
        .healthChecked(true)
        .registeredAt(LocalDate.parse("2024-02-06"))
        .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(adoptionRepository.findById(adoptionId)).thenReturn(Optional.of(adoption));

    // when
    bookmarkService.registerBookmark(userId, adoptionId);

    // then
    verify(bookmarkRepository).save(any(Bookmark.class));
  }

  @Test
  @DisplayName("[북마크 등록] - 회원 정보가 없을 때 UserNotFoundException 발생")
  void registerBookmark_when_verifyUser_then_UserNotFoundException() {
    // given
    long userId = 1L;
    long adoptionId = 1L;

    when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(any(User.class)));

    // when & then
    assertThrows(UserNotFoundException.class, () ->
        bookmarkService.registerBookmark(userId, adoptionId)
    );
  }

  @Test
  @DisplayName("[북마크 등록] - 입양 게시물이 없을 때 AdoptionNotFoundException 발생")
  void registerBookmark_when_no_adoption_then_AdoptionNotFoundException() {
    // given
    long userId = 1L;
    long adoptionId = 1L;

    User user = User.builder()
        .email("test@gmail.com")
        .nickname("반쪽이")
        .role(Set.of(Role.ROLE_USER))
        .loginType(LoginType.EMAIL)
        .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(adoptionRepository.findById(adoptionId)).thenReturn(Optional.ofNullable(any(Adoption.class)));

    // when & then
    assertThrows(AdoptionNotFoundException.class, () ->
        bookmarkService.registerBookmark(userId, adoptionId)
    );
  }

}