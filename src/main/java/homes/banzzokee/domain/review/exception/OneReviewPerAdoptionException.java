package homes.banzzokee.domain.review.exception;

import static homes.banzzokee.global.error.ErrorCode.ONLY_ONE_REVIEW_ALLOWED_PER_ADOPTION;

import homes.banzzokee.global.error.exception.CustomException;

public class OneReviewPerAdoptionException extends CustomException {

  public OneReviewPerAdoptionException() {
    super(ONLY_ONE_REVIEW_ALLOWED_PER_ADOPTION);
  }
}
