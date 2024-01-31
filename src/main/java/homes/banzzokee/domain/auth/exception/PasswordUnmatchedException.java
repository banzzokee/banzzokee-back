package homes.banzzokee.domain.auth.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.PASSWORD_UNMATCHED;

public class PasswordUnmatchedException extends CustomException {

  public PasswordUnmatchedException() {
    super(PASSWORD_UNMATCHED);
  }
}
