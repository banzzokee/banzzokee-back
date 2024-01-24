package homes.banzzokee.domain.user.dao;

import homes.banzzokee.domain.user.entity.Follow;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

  Optional<Follow> findByFolloweeIdAndFollowerId(long followeeId, long followerId);
}
