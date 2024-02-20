package homes.banzzokee.domain.notification.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationMessage {

  @JdbcTypeCode(SqlTypes.JSON)
  private Object message;

  public NotificationMessage(Object message) {
    this.message = message;
  }
}
