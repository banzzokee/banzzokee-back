package homes.banzzokee.infra.rabbitmq.event;

import static lombok.AccessLevel.PROTECTED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(force = true, access = PROTECTED)
public abstract class BaseMessage<T> {

  @JsonIgnore
  private final String routingKey;

  private final LocalDateTime issuedAt = LocalDateTime.now();

  private final T payload;
}
