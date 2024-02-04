package homes.banzzokee.domain.auth.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.EMAIL_UNMATCHED;


public class EmailUnmatchedException extends CustomException {

  public EmailUnmatchedException() {
    super(EMAIL_UNMATCHED);
  }
}
