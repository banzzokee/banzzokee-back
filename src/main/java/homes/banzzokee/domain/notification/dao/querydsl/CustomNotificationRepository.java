package homes.banzzokee.domain.notification.dao.querydsl;

import homes.banzzokee.domain.notification.dto.NotificationDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CustomNotificationRepository {

  Slice<NotificationDto> getNotificationList(Pageable pageable, Boolean checked,
      Long userId);

}
