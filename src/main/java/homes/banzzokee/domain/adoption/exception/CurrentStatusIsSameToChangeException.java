package homes.banzzokee.domain.adoption.exception;

import static homes.banzzokee.global.error.ErrorCode.CURRENT_STATUS_IS_SAME_TO_CHANGE;

import homes.banzzokee.global.error.exception.CustomException;

public class CurrentStatusIsSameToChangeException extends CustomException {

  public CurrentStatusIsSameToChangeException() {
    super(CURRENT_STATUS_IS_SAME_TO_CHANGE);
  }
}
