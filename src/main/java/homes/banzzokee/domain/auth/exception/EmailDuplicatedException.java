package homes.banzzokee.domain.auth.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.EMAIL_DUPLICATED;

public class EmailDuplicatedException extends CustomException {

  public EmailDuplicatedException() {
    super(EMAIL_DUPLICATED);
  }
}
