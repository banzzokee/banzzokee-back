package homes.banzzokee.global.security.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.TOKEN_REQUIRED;

public class TokenRequiredException extends CustomException {

  public TokenRequiredException() {
    super(TOKEN_REQUIRED);
  }
}
