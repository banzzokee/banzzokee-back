package homes.banzzokee.domain.auth.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.EMAIL_CODE_UNMATCHED;

public class EmailCodeUnmatchedException extends CustomException {

  public EmailCodeUnmatchedException() {
    super(EMAIL_CODE_UNMATCHED);
  }
}
