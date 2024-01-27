package homes.banzzokee.global.config.stomp.exception;

import homes.banzzokee.global.error.ErrorCode;
import homes.banzzokee.global.error.exception.CustomException;

/**
 * WebSocket 에러 처리를 위한 Exception
 */
public class SocketException extends CustomException {

  public SocketException(ErrorCode errorCode) {
    super(errorCode);
  }
}
