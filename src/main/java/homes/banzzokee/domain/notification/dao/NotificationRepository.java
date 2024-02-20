package homes.banzzokee.domain.notification.dao;

import homes.banzzokee.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {

}
