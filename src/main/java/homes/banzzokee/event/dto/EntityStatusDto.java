package homes.banzzokee.event.dto;

import homes.banzzokee.event.type.EntityAction;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EntityStatusDto {

  private final Long id;

  private final EntityAction action;

  public static EntityStatusDto of(Long id, EntityAction action) {
    return EntityStatusDto.builder()
        .id(id)
        .action(action)
        .build();
  }
}
