package homes.banzzokee.event;

import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.event.dto.EntityStatusDto;
import homes.banzzokee.event.type.EntityAction;
import homes.banzzokee.infra.rabbitmq.event.BaseMessage;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
public class EntityEvent extends BaseMessage<EntityStatusDto> {

  public static EntityEvent of(Long entityId, EntityAction action) {
    return EntityEvent.builder()
        .routingKey(action.getRoutingKey())
        .payload(EntityStatusDto.of(entityId, action))
        .build();
  }
}
