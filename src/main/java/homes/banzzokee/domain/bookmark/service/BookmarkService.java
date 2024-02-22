package homes.banzzokee.domain.bookmark.service;

import static homes.banzzokee.event.type.FcmTopicAction.SUBSCRIBE;
import static homes.banzzokee.event.type.FcmTopicAction.UNSUBSCRIBE;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.dto.AdoptionDto;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.bookmark.dao.BookmarkRepository;
import homes.banzzokee.domain.bookmark.dto.BookmarkRegisterRequest;
import homes.banzzokee.domain.bookmark.entity.Bookmark;
import homes.banzzokee.domain.bookmark.exception.BookmarkAdoptionNotExistException;
import homes.banzzokee.domain.bookmark.exception.BookmarkAlreadyExistsException;
import homes.banzzokee.domain.bookmark.exception.BookmarkNotFoundException;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.event.FcmTopicStatusChangeEvent;
import homes.banzzokee.global.error.exception.NoAuthorizedException;
import homes.banzzokee.global.security.UserDetailsImpl;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {

  private final UserRepository userRepository;
  private final BookmarkRepository bookmarkRepository;
  private final AdoptionRepository adoptionRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void registerBookmark(UserDetailsImpl userDetails,
      BookmarkRegisterRequest request) {
    User user = userRepository.findById(userDetails.getUserId())
        .orElseThrow(UserNotFoundException::new);
    Adoption adoption = adoptionRepository.findById(request.getAdoptionId())
        .orElseThrow(AdoptionNotFoundException::new);
    Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndAdoptionId(
        userDetails.getUserId(), request.getAdoptionId());
    if (existingBookmark.isPresent()) {
      throw new BookmarkAlreadyExistsException();
    }
    Bookmark bookmark = bookmarkRepository.save(Bookmark.builder()
        .user(user)
        .adoption(adoption)
        .build());
    eventPublisher.publishEvent(FcmTopicStatusChangeEvent.of(SUBSCRIBE, bookmark));
  }

  @Transactional
  public void deleteBookmark(UserDetailsImpl userDetails, long adoptionId) {
    Bookmark bookmark = bookmarkRepository.findByAdoptionId(adoptionId)
        .orElseThrow(BookmarkAdoptionNotExistException::new);
    if (!userDetails.getUserId().equals(bookmark.getUser().getId())) {
      throw new NoAuthorizedException();
    }
    bookmarkRepository.delete(bookmark);
    eventPublisher.publishEvent(FcmTopicStatusChangeEvent.of(UNSUBSCRIBE, bookmark));
  }

  @Transactional(readOnly = true)
  public Slice<AdoptionDto> findAllBookmark(UserDetailsImpl userDetails,
      Pageable pageable) {
    Slice<Bookmark> bookmarks = bookmarkRepository.findByUserId(userDetails.getUserId(),
        pageable);
    if (!bookmarks.hasContent()) {
      throw new BookmarkNotFoundException();
    }
    List<AdoptionDto> adoptionDtos = bookmarks.getContent().stream()
        .map(bookmark -> AdoptionDto.of(bookmark.getAdoption(), bookmark))
        .collect(Collectors.toList());
    return new SliceImpl<>(adoptionDtos, bookmarks.getPageable(), bookmarks.hasNext());
  }
}
