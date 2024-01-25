package homes.banzzokee.domain.user.exception;

import static homes.banzzokee.global.error.ErrorCode.CAN_NOT_FOLLOW_SELF;

import homes.banzzokee.global.error.exception.CustomException;

public class CanNotFollowSelfException extends CustomException {

  public CanNotFollowSelfException() {
    super(CAN_NOT_FOLLOW_SELF);
  }
}
