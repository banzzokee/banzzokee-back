package homes.banzzokee.global.config.stomp.exception;

import static homes.banzzokee.global.error.ErrorCode.INVALID_TOKEN;

public class SocketTokenInvalidException extends SocketException {

  public SocketTokenInvalidException() {
    super(INVALID_TOKEN);
  }
}
