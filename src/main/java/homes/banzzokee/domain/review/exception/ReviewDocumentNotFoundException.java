package homes.banzzokee.domain.review.exception;

import static homes.banzzokee.global.error.ErrorCode.REVIEW_DOCUMENT_NOT_FOUND;

import homes.banzzokee.global.error.exception.CustomException;

public class ReviewDocumentNotFoundException extends CustomException {

  public ReviewDocumentNotFoundException() {
    super(REVIEW_DOCUMENT_NOT_FOUND);
  }
}
