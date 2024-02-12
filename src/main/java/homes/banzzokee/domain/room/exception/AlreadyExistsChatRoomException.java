package homes.banzzokee.domain.room.exception;

import static homes.banzzokee.global.error.ErrorCode.CHAT_ROOM_ALREADY_EXIST;

import homes.banzzokee.global.error.exception.CustomException;

public class AlreadyExistsChatRoomException extends CustomException {

  public AlreadyExistsChatRoomException() {
    super(CHAT_ROOM_ALREADY_EXIST);
  }
}
