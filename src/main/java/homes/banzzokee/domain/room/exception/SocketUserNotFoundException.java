package homes.banzzokee.domain.room.exception;

import static homes.banzzokee.global.error.ErrorCode.USER_NOT_FOUND;

import homes.banzzokee.global.config.stomp.exception.SocketException;

public class SocketUserNotFoundException extends SocketException {

  public SocketUserNotFoundException() {
    super(USER_NOT_FOUND);
  }
}
