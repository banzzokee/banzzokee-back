package homes.banzzokee.domain.notification.dao;

import homes.banzzokee.domain.notification.entity.FcmToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

  Optional<FcmToken> findByToken(String token);

  List<FcmToken> findAllByUserId(Long userId);
}
