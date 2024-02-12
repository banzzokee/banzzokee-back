package homes.banzzokee.domain.bookmark.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.dto.AdoptionDto;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.bookmark.dao.BookmarkRepository;
import homes.banzzokee.domain.bookmark.dto.BookmarkRegisterRequest;
import homes.banzzokee.domain.bookmark.entity.Bookmark;
import homes.banzzokee.domain.bookmark.exception.BookmarkAlreadyExistsException;
import homes.banzzokee.domain.bookmark.exception.BookmarkNotFoundException;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

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
    if (!userDetails.getUserId().equals(bookmark.getUser().getId())) {
      throw new NoAuthorizedException();
    }
    bookmarkRepository.delete(bookmark);
  }

  @Transactional(readOnly = true)
  public Slice<AdoptionDto> findAllBookmark(UserDetailsImpl userDetails, Pageable pageable) {
    Slice<Bookmark> bookmarks = bookmarkRepository.findByUserId(userDetails.getUserId(), pageable);
    if (!bookmarks.hasContent()) {
      throw new BookmarkNotFoundException();
    }
    List<AdoptionDto> adoptionDtos = bookmarks.getContent().stream()
        .map(bookmark -> AdoptionDto.fromEntity(bookmark.getAdoption()))
        .collect(Collectors.toList());
    return new SliceImpl<>(adoptionDtos, bookmarks.getPageable(), bookmarks.hasNext());
  }
}
