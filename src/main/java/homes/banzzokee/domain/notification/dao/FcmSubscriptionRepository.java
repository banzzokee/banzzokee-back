package homes.banzzokee.domain.notification.dao;

import homes.banzzokee.domain.notification.entity.FcmSubscription;
import homes.banzzokee.domain.notification.entity.FcmToken;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmSubscriptionRepository extends
    JpaRepository<FcmSubscription, Long> {

  void deleteByFcmTokenIn(Collection<FcmToken> fcmTokens);
}
