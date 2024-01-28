package homes.banzzokee.domain.shelter.exception;

import static homes.banzzokee.global.error.ErrorCode.SHELTER_ALREADY_VERIFIED;

import homes.banzzokee.global.error.exception.CustomException;
import lombok.Getter;

@Getter
public class ShelterAlreadyVerifiedException extends CustomException {

  private final Long shelterId;

  public ShelterAlreadyVerifiedException(Long shelterId) {
    super(SHELTER_ALREADY_VERIFIED);
    this.shelterId = shelterId;
  }
}
