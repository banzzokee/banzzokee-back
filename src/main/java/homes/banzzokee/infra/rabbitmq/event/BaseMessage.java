package homes.banzzokee.infra.rabbitmq.event;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class BaseMessage<T> {

  private final LocalDateTime issuedAt = LocalDateTime.now();

  private final T payload;
}
