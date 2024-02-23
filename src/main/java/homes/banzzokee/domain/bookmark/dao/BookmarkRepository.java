package homes.banzzokee.domain.bookmark.dao;

import homes.banzzokee.domain.adoption.entity.Adoption;
import homes.banzzokee.domain.bookmark.entity.Bookmark;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  Optional<Bookmark> findByUserIdAndAdoptionId(Long userId, Long adoptionId);

  Slice<Bookmark> findByUserId(Long userId, Pageable pageable);

  @Modifying
  @Query("DELETE FROM Bookmark WHERE adoption = :adoption")
  void deleteByAdoption(@Param("adoption") Adoption adoption);

}
