package homes.banzzokee.domain.auth.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.CONFIRM_PASSWORD_UNMATCHED;

public class ConfirmPasswordUnMatchException extends CustomException {

  public ConfirmPasswordUnMatchException() {
    super(CONFIRM_PASSWORD_UNMATCHED);
  }
}
