package homes.banzzokee.domain.auth.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.EMAIL_NOT_FOUND;

public class EmailNotFoundException extends CustomException {

  public EmailNotFoundException() {
    super(EMAIL_NOT_FOUND);
  }
}
