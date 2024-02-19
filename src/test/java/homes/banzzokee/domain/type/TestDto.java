package homes.banzzokee.domain.type;

import homes.banzzokee.domain.type.AdoptionStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestDto {
  private final Long adoptionId;
  private final AdoptionStatus status;
}
