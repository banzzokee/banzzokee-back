package homes.banzzokee.event.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdoptionAction {
  CREATE("adoption.created"),
  UPDATE("adoption.updated"),
  DELETE("adoption.deleted"),
  STATUS("adoption.status.changed");

  private final String routingKey;
}
