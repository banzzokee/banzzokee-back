package homes.banzzokee.global.security.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.ACCESS_TOKEN_EXPIRED;

public class AccessTokenExpiredException extends CustomException {

  public AccessTokenExpiredException() {
    super(ACCESS_TOKEN_EXPIRED);
  }
}
