package homes.banzzokee.domain.auth.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.EMAIL_CODE_INVALID;

public class EmailCodeInvalidException extends CustomException {

  public EmailCodeInvalidException() {
    super(EMAIL_CODE_INVALID);
  }
}
