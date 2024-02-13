package homes.banzzokee.global.config.stomp.exception;

import static homes.banzzokee.global.error.ErrorCode.ACCESS_TOKEN_EXPIRED;

import homes.banzzokee.global.error.ErrorCode;

public class SocketAccessTokenExpiredException extends SocketException {

  public SocketAccessTokenExpiredException() {
    super(ACCESS_TOKEN_EXPIRED);
  }
}
