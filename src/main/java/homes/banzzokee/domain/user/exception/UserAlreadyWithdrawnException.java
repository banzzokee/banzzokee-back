package homes.banzzokee.domain.user.exception;

import static homes.banzzokee.global.error.ErrorCode.USER_ALREADY_WITHDRAWN;

import homes.banzzokee.global.error.exception.CustomException;

public class UserAlreadyWithdrawnException extends CustomException {

  public UserAlreadyWithdrawnException() {
    super(USER_ALREADY_WITHDRAWN);
  }
}
