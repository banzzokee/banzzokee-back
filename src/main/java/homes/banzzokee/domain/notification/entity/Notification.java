package homes.banzzokee.domain.notification.entity;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.domain.common.entity.BaseEntity;
import homes.banzzokee.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor(access = PROTECTED, force = true)
public class Notification extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(columnDefinition = "json")
  private final String message;

  @OneToMany(mappedBy = "notification", cascade = ALL)
  private final List<NotificationReceiver> receivers;

  public static Notification of(String message, List<User> users) {
    Notification notification = Notification.builder()
        .message(message)
        .receivers(new ArrayList<>())
        .build();

    users.forEach(user ->
    {
      NotificationReceiver receiver = NotificationReceiver.of(user);
      notification.addReceiver(receiver);
    });

    return notification;
  }

  private void addReceiver(NotificationReceiver receiver) {
    this.receivers.add(receiver);
    receiver.setNotification(this);
  }
}
