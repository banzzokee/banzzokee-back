package homes.banzzokee.domain.bookmark.service;

import static homes.banzzokee.domain.type.Role.ROLE_USER;
import static homes.banzzokee.event.type.FcmTopicAction.SUBSCRIBE;
import static homes.banzzokee.event.type.FcmTopicAction.UNSUBSCRIBE;
import static homes.banzzokee.event.type.FcmTopicCategory.ADOPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.dto.AdoptionDto;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.bookmark.dao.BookmarkRepository;
import homes.banzzokee.domain.bookmark.dto.BookmarkRegisterRequest;
import homes.banzzokee.domain.bookmark.entity.Bookmark;
import homes.banzzokee.domain.bookmark.exception.BookmarkAlreadyExistsException;
import homes.banzzokee.domain.bookmark.exception.BookmarkNotFoundException;
import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.domain.type.LoginType;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.event.FcmTopicStatusChangeEvent;
import homes.banzzokee.event.dto.FcmTopicStatusDto;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import homes.banzzokee.global.security.UserDetailsImpl;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Test
  @DisplayName("[북마크 등록] - 성공 검증")
  void registerBookmark_when_success_then_verify() {
    // given
    User user = spy(User.builder()
        .email("test@gmail.com")
        .nickname("반쪽이")
        .role(Set.of(ROLE_USER))
        .loginType(LoginType.EMAIL)
        .build());
    when(user.getId()).thenReturn(1L);

    Adoption adoption = spy(Adoption.builder()
        .title("강아지")
        .content("강아지 입니다.")
        .breed(BreedType.POODLE)
        .size(DogSize.SMALL)
        .neutering(true)
        .gender(DogGender.MALE)
        .age(1)
        .healthChecked(true)
        .registeredAt(LocalDate.parse("2024-02-06"))
        .build());
    when(adoption.getId()).thenReturn(1L);

    Bookmark bookmark = spy(Bookmark.builder()
        .user(user)
        .adoption(adoption)
        .build());

    BookmarkRegisterRequest bookmarkRegisterRequest = BookmarkRegisterRequest.builder()
        .adoptionId(1L)
        .build();

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(adoptionRepository.findById(bookmarkRegisterRequest.getAdoptionId())).thenReturn(
        Optional.of(adoption));
    when(bookmarkRepository.findByUserIdAndAdoptionId(1L,
        bookmarkRegisterRequest.getAdoptionId())).thenReturn(Optional.empty());
    when(bookmarkRepository.save(any(Bookmark.class))).thenReturn(bookmark);

    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
    given(userDetails.getUserId()).willReturn(1L);

    // when
    bookmarkService.registerBookmark(userDetails, bookmarkRegisterRequest);

    // then
    verify(bookmarkRepository).save(any(Bookmark.class));
    ArgumentCaptor<FcmTopicStatusChangeEvent> eventCaptor
        = ArgumentCaptor.forClass(FcmTopicStatusChangeEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    FcmTopicStatusDto payload = eventCaptor.getValue().getPayload();
    assertEquals(SUBSCRIBE, payload.getAction());
    assertEquals(ADOPTION, payload.getTopicCategory());
    assertEquals("topic.adoption." + adoption.getId(), payload.getTopic());
    assertEquals(adoption.getId(), payload.getTopicId());
    assertEquals(user.getId(), payload.getUserId());
  }

  @Test
  @DisplayName("[북마크 등록] - 회원 정보가 없는 경우 UserNotFoundException 발생")
  void registerBookmark_when_verifyUser_then_UserNotFoundException() {
    // given
    User user = User.builder()
        .email("test@gmail.com")
        .nickname("반쪽이")
        .role(Set.of(ROLE_USER))
        .loginType(LoginType.EMAIL)
        .build();
    UserDetailsImpl userDetails = new UserDetailsImpl(user,
        List.of(new SimpleGrantedAuthority(ROLE_USER.name())));
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
  @DisplayName("[북마크 등록] - 입양 게시물이 없는 경우 AdoptionNotFoundException 발생")
  void registerBookmark_when_noAdoption_then_AdoptionNotFoundException() {
    // given
    User user = User.builder()
        .email("test@gmail.com")
        .nickname("반쪽이")
        .role(Set.of(ROLE_USER))
        .loginType(LoginType.EMAIL)
        .build();
    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
    given(userDetails.getUserId()).willReturn(1L);
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

    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
    given(userDetails.getUserId()).willReturn(1L);
    BookmarkRegisterRequest bookmarkRegisterRequest = new BookmarkRegisterRequest(1L);

    when(userRepository.findById(userDetails.getUserId())).thenReturn(Optional.of(user));
    when(adoptionRepository.findById(bookmarkRegisterRequest.getAdoptionId())).thenReturn(
        Optional.of(adoption));
    when(bookmarkRepository.findByUserIdAndAdoptionId(userDetails.getUserId(),
        bookmarkRegisterRequest.getAdoptionId())).thenReturn(Optional.of(
        new Bookmark(user, adoption)));

    // when & then
    assertThrows(BookmarkAlreadyExistsException.class, () ->
        bookmarkService.registerBookmark(userDetails, bookmarkRegisterRequest));
  }

  @Test
  @DisplayName("[북마크 삭제] - 성공 검증")
  void deleteBookmark_when_success_then_verify() {
    // given
    User user = mock(User.class);
    when(user.getId()).thenReturn(1L);

    Adoption adoption = mock(Adoption.class);
    when(adoption.getId()).thenReturn(1L);

    Bookmark bookmark = Bookmark.builder()
        .user(user)
        .adoption(adoption)
        .build();
    given(bookmarkRepository.findById(1L)).willReturn(Optional.of(bookmark));

    UserDetailsImpl userDetails = new UserDetailsImpl(user, Collections
        .singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    // when
    bookmarkService.deleteBookmark(userDetails, 1L);

    // then
    ArgumentCaptor<Bookmark> bookmarkCaptor = ArgumentCaptor.forClass(Bookmark.class);
    verify(bookmarkRepository).delete(bookmarkCaptor.capture());
    assertEquals(bookmark, bookmarkCaptor.getValue());

    ArgumentCaptor<FcmTopicStatusChangeEvent> eventCaptor
        = ArgumentCaptor.forClass(FcmTopicStatusChangeEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    FcmTopicStatusDto payload = eventCaptor.getValue().getPayload();
    assertEquals(UNSUBSCRIBE, payload.getAction());
    assertEquals(ADOPTION, payload.getTopicCategory());
    assertEquals("topic.adoption." + adoption.getId(), payload.getTopic());
    assertEquals(adoption.getId(), payload.getTopicId());
    assertEquals(user.getId(), payload.getUserId());
  }

  @Test
  @DisplayName("[북마크 삭제] - 저장된 북마크가 없는 경우 BookmarkNotFoundException 발생")
  void deleteBookmark_when_bookmarkNotFound_then_BookmarkNotFoundException() {
    // given
    long bookmarkId = 1L;
    User user = User.builder()
        .email("test@gmail.com")
        .nickname("반쪽이")
        .role(Collections.singleton(ROLE_USER))
        .loginType(LoginType.EMAIL)
        .build();
    given(bookmarkRepository.findById(bookmarkId)).willReturn(Optional.empty());

    UserDetailsImpl userDetails = new UserDetailsImpl(user,
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

    // when & then
    assertThrows(BookmarkNotFoundException.class, () ->
        bookmarkService.deleteBookmark(userDetails, bookmarkId));
  }

  @Test
  @DisplayName("[북마크 삭제] - 권한 정보가 없는 경우 NoAuthorizedException 발생")
  void deleteBookmark_when_noAuthorized_then_NoAuthorizedException() {
    // given
    long bookmarkId = 1L;
    User user = mock(User.class);
    given(user.getId()).willReturn(2L);

    Bookmark bookmark = Bookmark.builder()
        .user(user)
        .build();
    given(bookmarkRepository.findById(bookmarkId)).willReturn(Optional.of(bookmark));

    // when
    User mockUser = mock(User.class);
    given(mockUser.getId()).willReturn(1L);
    UserDetailsImpl userDetails = new UserDetailsImpl(mockUser,
        Collections.singletonList(new SimpleGrantedAuthority("USER_ROLE")));

    // then
    assertThrows(NoAuthorizedException.class, () ->
        bookmarkService.deleteBookmark(userDetails, 1L));
  }

  @Test
  @DisplayName("[북마크 전체 조회] - 성공 검증")
  void findAllBookmark_when_verify_then_success() {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    User user = mock(User.class);
    given(user.getId()).willReturn(1L);

    Adoption adoption = mock(Adoption.class);
    given(adoption.getUser()).willReturn(user);

    Bookmark bookmark1 = mock(Bookmark.class);
    given(bookmark1.getAdoption()).willReturn(adoption);

    Bookmark bookmark2 = mock(Bookmark.class);
    given(bookmark2.getAdoption()).willReturn(adoption);

    UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
    given(userDetails.getUserId()).willReturn(1L);

    List<Bookmark> bookmarksList = Arrays.asList(bookmark1, bookmark2);
    Slice<Bookmark> bookmarksSlice = new SliceImpl<>(bookmarksList, pageable, true);
    given(bookmarkRepository.findByUserId(userDetails.getUserId(), pageable)).willReturn(
        bookmarksSlice);

    // when
    Slice<AdoptionDto> result = bookmarkService.findAllBookmark(userDetails, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.hasNext()).isTrue();
  }
}