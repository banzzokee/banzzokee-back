package homes.banzzokee.domain.adoption.exception;

import static homes.banzzokee.global.error.ErrorCode.ADOPTION_NOT_FOUND;

import homes.banzzokee.global.error.exception.CustomException;

public class AdoptionNotFoundException extends CustomException {
  public AdoptionNotFoundException() {
    super(ADOPTION_NOT_FOUND);
  }
}
