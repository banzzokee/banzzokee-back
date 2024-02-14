package homes.banzzokee.domain.adoption.exception;

import homes.banzzokee.global.error.ErrorCode;
import homes.banzzokee.global.error.exception.CustomException;

public class AssignedUserMustBeNullException extends CustomException {

  public AssignedUserMustBeNullException() {
    super(ErrorCode.ASSIGNED_USER_MUST_BE_NULL);
  }
}
