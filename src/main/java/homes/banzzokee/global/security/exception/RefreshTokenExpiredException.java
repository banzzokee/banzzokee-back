package homes.banzzokee.global.security.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.REFRESH_TOKEN_EXPIRED;

public class RefreshTokenExpiredException extends CustomException {

  public RefreshTokenExpiredException() {
    super(REFRESH_TOKEN_EXPIRED);
  }
}
