package homes.banzzokee.domain.user.dao;

import homes.banzzokee.domain.user.entity.Follow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

  Optional<Follow> findByFolloweeIdAndFollowerId(long followeeId, long followerId);
  List<Follow> findAllByFollowerId(long followeeId, Pageable pageable);
}
