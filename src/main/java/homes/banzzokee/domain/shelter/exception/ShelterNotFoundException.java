package homes.banzzokee.domain.shelter.exception;

import static homes.banzzokee.global.error.ErrorCode.SHELTER_NOT_FOUND;

import homes.banzzokee.global.error.exception.CustomException;
import lombok.Getter;

@Getter
public class ShelterNotFoundException extends CustomException {

  public ShelterNotFoundException() {
    super(SHELTER_NOT_FOUND);
  }
}
