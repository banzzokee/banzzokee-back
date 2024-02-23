package homes.banzzokee.global.config.stomp.exception;

import static homes.banzzokee.global.error.ErrorCode.NO_AUTHORIZED;

public class SocketNoAuthorizedException extends SocketException {

  public SocketNoAuthorizedException() {
    super(NO_AUTHORIZED);
  }
}
