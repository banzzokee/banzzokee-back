package homes.banzzokee.global.security.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.INVALID_TOKEN;

public class TokenInvalidException extends CustomException {

  public TokenInvalidException() {
    super(INVALID_TOKEN);
  }
}
