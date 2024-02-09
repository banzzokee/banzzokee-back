package homes.banzzokee.domain.bookmark.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.bookmark.dao.BookmarkRepository;
import homes.banzzokee.domain.bookmark.dto.BookmarkRegisterRequest;
import homes.banzzokee.domain.bookmark.entity.Bookmark;
import homes.banzzokee.domain.bookmark.exception.BookmarkAlreadyExistsException;
import homes.banzzokee.domain.type.*;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.security.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static homes.banzzokee.domain.type.Role.ROLE_USER;
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

  @Test
  @DisplayName("[북마크 등록] - 성공 검증")
  void registerBookmark_when_success_then_verify() {
    // given
    User user = User.builder()
        .email("test@gmail.com")
        .nickname("반쪽이")
        .role(Set.of(ROLE_USER))
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

    BookmarkRegisterRequest bookmarkRegisterRequest = BookmarkRegisterRequest.builder()
        .adoptionId(1L)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(adoptionRepository.findById(bookmarkRegisterRequest.getAdoptionId())).thenReturn(Optional.of(adoption));
    when(bookmarkRepository.findByUserIdAndAdoptionId(1L, bookmarkRegisterRequest.getAdoptionId())).thenReturn(Optional.empty());

    UserDetailsImpl userDetails = new UserDetailsImpl(1L, "test@gmail.com", Collections.emptyList());

    // when
    bookmarkService.registerBookmark(userDetails, bookmarkRegisterRequest);

    // then
    verify(bookmarkRepository).save(any(Bookmark.class));

  }

  @Test
  @DisplayName("[북마크 등록] - 회원 정보가 없을 때 UserNotFoundException 발생")
  void registerBookmark_when_verifyUser_then_UserNotFoundException() {
    // given
    UserDetailsImpl userDetails = new UserDetailsImpl(
        1L, "test@gmail.com", Collections.emptyList());
    BookmarkRegisterRequest bookmarkRegisterRequest = BookmarkRegisterRequest
        .builder()
        .adoptionId(1L)
        .build();

    when(userRepository.findById(userDetails.getUserId())).thenReturn(Optional.empty());

    // when & then
    assertThrows(UserNotFoundException.class, () ->
        bookmarkService.registerBookmark(userDetails, bookmarkRegisterRequest)
    );
  }

  @Test
  @DisplayName("[북마크 등록] - 입양 게시물이 없을 때 AdoptionNotFoundException 발생")
  void registerBookmark_when_no_adoption_then_AdoptionNotFoundException() {
    // given
    User user = User.builder()
        .email("test@gmail.com")
        .nickname("반쪽이")
        .role(Set.of(ROLE_USER))
        .loginType(LoginType.EMAIL)
        .build();
    UserDetailsImpl userDetails = new UserDetailsImpl(
        1L, "test@gmail.com", Collections.emptyList());
    BookmarkRegisterRequest bookmarkRegisterRequest = BookmarkRegisterRequest
        .builder()
        .adoptionId(1L)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(adoptionRepository.findById(bookmarkRegisterRequest.getAdoptionId())).
        thenReturn(Optional.empty());

    // when & then
    assertThrows(AdoptionNotFoundException.class, () ->
        bookmarkService.registerBookmark(userDetails, bookmarkRegisterRequest)
    );
  }

  @Test
  @DisplayName("[북마크 등록] - 이미 저장된 북마크를 등록할 때 BookmarkAlreadyExistsException 발생")
  void registerBookmark_when_existAlreadyBookmark_then_BookmarkAlreadyExistsException() {
    // given
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

    UserDetailsImpl userDetails = new UserDetailsImpl(
        1L, "test@gmail.com", Collections.emptyList());
    BookmarkRegisterRequest bookmarkRegisterRequest = new BookmarkRegisterRequest(1L);

    when(userRepository.findById(userDetails.getUserId())).thenReturn(Optional.of(user));
    when(adoptionRepository.findById(bookmarkRegisterRequest.getAdoptionId())).thenReturn(Optional.of(adoption));
    when(bookmarkRepository.findByUserIdAndAdoptionId(userDetails.getUserId(),
        bookmarkRegisterRequest.getAdoptionId())).thenReturn(Optional.of(
        new Bookmark(user, adoption)));

    // when & then
    assertThrows(BookmarkAlreadyExistsException.class, () ->
        bookmarkService.registerBookmark(userDetails, bookmarkRegisterRequest));
  }
}