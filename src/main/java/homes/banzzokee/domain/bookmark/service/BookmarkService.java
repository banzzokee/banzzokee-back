package homes.banzzokee.domain.bookmark.service;

import homes.banzzokee.domain.adoption.dao.AdoptionRepository;
import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.adoption.exception.AdoptionNotFoundException;
import homes.banzzokee.domain.bookmark.dao.BookmarkRepository;
import homes.banzzokee.domain.bookmark.entity.Bookmark;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkService {

  private final UserRepository userRepository;
  private final BookmarkRepository bookmarkRepository;
  private final AdoptionRepository adoptionRepository;

  public void registerBookmark(long userId, long adoptionId) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
    Adoption adoption = adoptionRepository.findById(adoptionId)
        .orElseThrow(AdoptionNotFoundException::new);
    bookmarkRepository.save(Bookmark.builder()
        .user(user)
        .adoption(adoption)
        .build());
  }
}
