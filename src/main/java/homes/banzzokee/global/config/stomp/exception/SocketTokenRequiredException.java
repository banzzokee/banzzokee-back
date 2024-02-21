package homes.banzzokee.global.config.stomp.exception;

import static homes.banzzokee.global.error.ErrorCode.TOKEN_REQUIRED;

public class SocketTokenRequiredException extends SocketException {

  public SocketTokenRequiredException() {
    super(TOKEN_REQUIRED);
  }
}
