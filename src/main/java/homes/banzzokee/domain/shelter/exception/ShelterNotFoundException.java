package homes.banzzokee.domain.shelter.exception;

import static homes.banzzokee.global.error.ErrorCode.SHELTER_NOT_FOUND;

import homes.banzzokee.global.error.exception.CustomException;
import lombok.Getter;

@Getter
public class ShelterNotFoundException extends CustomException {

  private final Long shelterId;

  public ShelterNotFoundException(Long shelterId) {
    super(SHELTER_NOT_FOUND);
    this.shelterId = shelterId;
  }
}
