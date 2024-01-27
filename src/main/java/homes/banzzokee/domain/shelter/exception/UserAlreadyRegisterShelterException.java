package homes.banzzokee.domain.shelter.exception;

import static homes.banzzokee.global.error.ErrorCode.USER_ALREADY_REGISTER_SHELTER;

import homes.banzzokee.global.error.exception.CustomException;

public class UserAlreadyRegisterShelterException extends CustomException {

  public UserAlreadyRegisterShelterException() {
    super(USER_ALREADY_REGISTER_SHELTER);
  }
}
