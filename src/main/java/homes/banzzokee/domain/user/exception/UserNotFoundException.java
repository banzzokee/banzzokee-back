package homes.banzzokee.domain.user.exception;

import static homes.banzzokee.global.error.ErrorCode.USER_NOT_FOUND;

import homes.banzzokee.global.error.exception.CustomException;

public class UserNotFoundException extends CustomException {

  public UserNotFoundException() {
    super(USER_NOT_FOUND);
  }
}
