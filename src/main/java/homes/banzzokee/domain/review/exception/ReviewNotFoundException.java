package homes.banzzokee.domain.review.exception;

import static homes.banzzokee.global.error.ErrorCode.REVIEW_NOT_FOUND;

import homes.banzzokee.global.error.exception.CustomException;

public class ReviewNotFoundException extends CustomException {

  public ReviewNotFoundException() {
    super(REVIEW_NOT_FOUND);
  }
}
