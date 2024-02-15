package homes.banzzokee.event;

import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.event.dto.EntityStatusDto;
import homes.banzzokee.event.type.EntityAction;
import homes.banzzokee.infra.rabbitmq.event.BaseMessage;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
public class AdoptionEvent extends BaseMessage<EntityStatusDto> {

  public static AdoptionEvent of(Long adoptionId, EntityAction action) {
    String routingKey = switch (action) {
      case CREATE -> "adoption.created";
      case UPDATE -> "adoption.updated";
      case DELETE -> "adoption.deleted";
    };

    return AdoptionEvent.builder()
        .routingKey(routingKey)
        .payload(EntityStatusDto.of(adoptionId, action))
        .build();
  }
}
