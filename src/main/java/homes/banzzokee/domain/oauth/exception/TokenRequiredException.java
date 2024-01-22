package homes.banzzokee.domain.oauth.exception;

import homes.banzzokee.global.error.ErrorCode;
import homes.banzzokee.global.error.exception.CustomException;

public class TokenRequiredException extends CustomException {

  public TokenRequiredException(ErrorCode errorCode) {
    super(errorCode);
  }
}