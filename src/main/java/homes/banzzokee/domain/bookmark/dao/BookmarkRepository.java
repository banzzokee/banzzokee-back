package homes.banzzokee.domain.bookmark.dao;

import homes.banzzokee.domain.bookmark.entity.Bookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  Optional<Bookmark> findByUserIdAndAdoptionId(Long userId, Long adoptionId);

  Slice<Bookmark> findByUserId(Long userId, Pageable pageable);
}
