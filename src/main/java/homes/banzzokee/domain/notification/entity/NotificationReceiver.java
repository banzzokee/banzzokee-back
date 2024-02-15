package homes.banzzokee.domain.notification.entity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.domain.common.entity.BaseEntity;
import homes.banzzokee.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor(access = PROTECTED, force = true)
public class NotificationReceiver extends BaseEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "notification_id")
  @Setter
  private Notification notification;

  @ManyToOne(fetch = LAZY)
  private final User user;

  private boolean checked;

  public static NotificationReceiver of(User user) {
    return NotificationReceiver.builder()
        .user(user)
        .checked(false)
        .build();
  }
}
