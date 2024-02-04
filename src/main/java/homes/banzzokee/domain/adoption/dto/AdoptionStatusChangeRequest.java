package homes.banzzokee.domain.adoption.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class AdoptionStatusChangeRequest {

  private final Long assignedUserId;

  @Pattern(regexp = "^[분양중|예약중|분양완료]$", message = "유효한 상태값이 아닙니다.")
  private final String status;

}
