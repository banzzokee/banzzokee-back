package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.domain.type.AdoptionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class AdoptionStatusChangeRequest {

  private final Long assignedUserId;

  @NotNull
  private final AdoptionStatus status;

}
