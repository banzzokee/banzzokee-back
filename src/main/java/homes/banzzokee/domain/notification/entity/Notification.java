package homes.banzzokee.domain.notification.entity;

import static jakarta.persistence.CascadeType.ALL;
import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.domain.common.entity.BaseEntity;
import homes.banzzokee.domain.user.entity.User;
import jakarta.persistence.Entity;
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
  private String id;

  private final String messageId;

  private final String topic;

  private final String title;

  private final String body;

  private final String image;

  @OneToMany(mappedBy = "notification", cascade = ALL)
  private final List<NotificationReceiver> receivers;

  public static Notification of(String id, String messageId, String topic, String title,
      String body, String image, List<User> users) {
    Notification notification = Notification.builder()
        .id(id)
        .messageId(messageId)
        .topic(topic)
        .title(title)
        .body(body)
        .image(image)
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
