package homes.banzzokee.domain.adoption.exception;

import static homes.banzzokee.global.error.ErrorCode.NEED_TO_ASSIGNED_USER_INFO;

import homes.banzzokee.global.error.exception.CustomException;

public class MustInputAssignedUserInfoException extends CustomException {

  public MustInputAssignedUserInfoException() {
    super(NEED_TO_ASSIGNED_USER_INFO);
  }
}
