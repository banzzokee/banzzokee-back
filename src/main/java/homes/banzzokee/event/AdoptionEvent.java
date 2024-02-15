package homes.banzzokee.event;

import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.event.dto.EntityStatusDto;
import homes.banzzokee.event.type.AdoptionAction;
import homes.banzzokee.infra.rabbitmq.event.BaseMessage;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
public class AdoptionEvent extends BaseMessage<EntityStatusDto> {

  public static AdoptionEvent of(Long adoptionId, AdoptionAction action) {
    return AdoptionEvent.builder()
        .routingKey(action.getRoutingKey())
        .payload(EntityStatusDto.of(adoptionId, action))
        .build();
  }
}
