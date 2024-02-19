package homes.banzzokee.domain.review.exception;

import homes.banzzokee.global.error.ErrorCode;
import homes.banzzokee.global.error.exception.CustomException;

public class DeletedReviewException extends CustomException {

  public DeletedReviewException() {
    super(ErrorCode.REVIEW_IS_DELETED);
  }
}
