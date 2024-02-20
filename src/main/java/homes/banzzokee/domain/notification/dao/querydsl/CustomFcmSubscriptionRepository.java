package homes.banzzokee.domain.notification.dao.querydsl;

import homes.banzzokee.domain.user.entity.User;
import java.util.List;

public interface CustomFcmSubscriptionRepository {

  List<User> findUsersByTopic(String topic);
}
