package homes.banzzokee.domain.room.exception;

import static homes.banzzokee.global.error.ErrorCode.ROOM_NOT_FOUND;

import homes.banzzokee.global.config.stomp.exception.SocketException;

public class SocketRoomNotFoundException extends SocketException {

  public SocketRoomNotFoundException() {
    super(ROOM_NOT_FOUND);
  }
}
