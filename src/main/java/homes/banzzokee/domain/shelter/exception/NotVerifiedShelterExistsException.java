package homes.banzzokee.domain.shelter.exception;

import static homes.banzzokee.global.error.ErrorCode.NOT_VERIFIED_SHELTER_EXISTS;

import homes.banzzokee.global.error.exception.CustomException;

public class NotVerifiedShelterExistsException extends CustomException {

  public NotVerifiedShelterExistsException() {
    super(NOT_VERIFIED_SHELTER_EXISTS);
  }
}
