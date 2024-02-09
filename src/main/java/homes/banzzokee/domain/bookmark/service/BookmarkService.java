package homes.banzzokee.domain.bookmark.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.bookmark.dao.BookmarkRepository;
import homes.banzzokee.domain.bookmark.dto.BookmarkRegisterRequest;
import homes.banzzokee.domain.bookmark.entity.Bookmark;
import homes.banzzokee.domain.bookmark.exception.BookmarkAlreadyExistsException;
import homes.banzzokee.domain.bookmark.exception.BookmarkNotFoundException;
import homes.banzzokee.domain.type.LoginType;
import homes.banzzokee.domain.type.Role;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import homes.banzzokee.global.security.exception.SocialLoginAuthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static homes.banzzokee.domain.type.LoginType.EMAIL;
import static homes.banzzokee.domain.type.LoginType.GOOGLE;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {

  private final UserRepository userRepository;
  private final BookmarkRepository bookmarkRepository;
  private final AdoptionRepository adoptionRepository;

  @Transactional
  public void registerBookmark(UserDetailsImpl userDetails, BookmarkRegisterRequest bookmarkRegisterRequest) {
    User user = userRepository.findById(userDetails.getUserId())
        .orElseThrow(UserNotFoundException::new);
    Adoption adoption = adoptionRepository.findById(bookmarkRegisterRequest.getAdoptionId())
        .orElseThrow(AdoptionNotFoundException::new);
    Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndAdoptionId(
        userDetails.getUserId(), bookmarkRegisterRequest.getAdoptionId());
    if (existingBookmark.isPresent()) {
      throw new BookmarkAlreadyExistsException();
    }
    bookmarkRepository.save(Bookmark.builder()
        .user(user)
        .adoption(adoption)
        .build());
  }

  @Transactional
  public void deleteBookmark(UserDetailsImpl userDetails, long bookmarkId) {
    Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
        .orElseThrow(BookmarkNotFoundException::new);
    boolean hasRole = userDetails.getAuthorities().stream()
        .anyMatch(authority ->
            bookmark.getUser().getRole().stream()
                .map(Role::name)
                .anyMatch(role -> role.contains(authority.getAuthority())));
    if (!hasRole) {
      throw new NoAuthorizedException();
    }
    Set<LoginType> loginTypes = Set.of(GOOGLE, EMAIL);
    if (!loginTypes.contains(bookmark.getUser().getLoginType())) {
      throw new SocialLoginAuthorizedException();
    }
    bookmarkRepository.delete(bookmark);
  }
}
