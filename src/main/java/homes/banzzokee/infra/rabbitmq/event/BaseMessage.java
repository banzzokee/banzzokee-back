package homes.banzzokee.infra.rabbitmq.event;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(force = true, access = PROTECTED)
public abstract class BaseMessage<T> {

  private final LocalDateTime issuedAt = LocalDateTime.now();

  private final T payload;
}
