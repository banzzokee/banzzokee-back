package homes.banzzokee.domain.notification.dao.querydsl;

import static homes.banzzokee.domain.notification.entity.QNotification.notification;
import static homes.banzzokee.domain.notification.entity.QNotificationReceiver.notificationReceiver;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import homes.banzzokee.domain.notification.dto.NotificationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class CustomNotificationRepositoryImpl implements
    CustomNotificationRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<NotificationDto> getNotificationList(Pageable pageable,
      Boolean checked,
      Long userId) {

    BooleanBuilder builder = new BooleanBuilder();
    if (checked != null) {
      builder.and(notificationReceiver.checked.eq(checked));
    }

    List<NotificationDto> contents = queryFactory
        .select(Projections.constructor(NotificationDto.class,
            notificationReceiver.notification.id,
            notificationReceiver.notification.message,
            notificationReceiver.notification.createdAt))
        .from(notificationReceiver)
        .join(notification)
        .fetchJoin()
        .on(notification.id.eq(notificationReceiver.notification.id)
            .and(notificationReceiver.user.id.eq(userId)))
        .where(builder)
        .orderBy(notification.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    boolean hasNext = false;
    if (contents.size() > pageable.getPageSize()) {
      contents.remove(pageable.getPageSize());
      hasNext = true;
    }

    return new SliceImpl<>(contents, pageable, hasNext);
  }

  @Override
  public void checkNotification(Long notificationId, Long userId) {
    queryFactory.update(notificationReceiver)
        .set(notificationReceiver.checked, true)
        .where(notificationReceiver.notification.id.eq(notificationId)
            .and(notificationReceiver.user.id.eq(userId)))
        .execute();
  }

  @Override
  public void checkAllNotifications(Long userId) {
    queryFactory.update(notificationReceiver)
        .set(notificationReceiver.checked, true)
        .where(notificationReceiver.user.id.eq(userId)
            .and(notificationReceiver.checked.eq(false)))
        .execute();
  }
}
