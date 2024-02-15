package homes.banzzokee.event.dto;

import homes.banzzokee.event.type.AdoptionAction;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EntityStatusDto {

  private final Long id;

  private final AdoptionAction action;

  public static EntityStatusDto of(Long id, AdoptionAction action) {
    return EntityStatusDto.builder()
        .id(id)
        .action(action)
        .build();
  }
}
