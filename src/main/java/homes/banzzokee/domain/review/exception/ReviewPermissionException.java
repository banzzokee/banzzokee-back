package homes.banzzokee.domain.review.exception;

import static homes.banzzokee.global.error.ErrorCode.REVIEW_CAN_BE_WRITE_BY_ASSIGNED_USER;

import homes.banzzokee.global.error.exception.CustomException;

public class ReviewPermissionException extends CustomException {

  public ReviewPermissionException() {
    super(REVIEW_CAN_BE_WRITE_BY_ASSIGNED_USER);
  }
}
