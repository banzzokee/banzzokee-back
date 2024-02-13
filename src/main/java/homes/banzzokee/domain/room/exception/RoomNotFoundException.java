package homes.banzzokee.domain.room.exception;

import static homes.banzzokee.global.error.ErrorCode.ROOM_NOT_FOUND;

import homes.banzzokee.global.error.exception.CustomException;

public class RoomNotFoundException extends CustomException {

  public RoomNotFoundException() {
    super(ROOM_NOT_FOUND);
  }
}
