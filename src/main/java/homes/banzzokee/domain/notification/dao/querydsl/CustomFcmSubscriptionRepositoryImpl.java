package homes.banzzokee.domain.notification.dao.querydsl;

import static homes.banzzokee.domain.notification.entity.QFcmSubscription.fcmSubscription;
import static homes.banzzokee.domain.user.entity.QUser.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import homes.banzzokee.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomFcmSubscriptionRepositoryImpl implements
    CustomFcmSubscriptionRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<User> findUsersByTopic(String topic) {
    return queryFactory.selectFrom(user)
        .join(fcmSubscription)
        .on(fcmSubscription.fcmToken.user.eq(user)
            .and(fcmSubscription.topic.eq(topic)))
        .select(user)
        .fetch();
  }
}
