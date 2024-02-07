package homes.banzzokee.global.config.stomp.exception;

import homes.banzzokee.global.error.ErrorCode;

public class SocketNoAuthorizedException extends SocketException {

  public SocketNoAuthorizedException() {
    super(ErrorCode.NO_AUTHORIZED);
  }
}
