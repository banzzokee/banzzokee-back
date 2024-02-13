package homes.banzzokee.domain.adoption.exception;

import static homes.banzzokee.global.error.ErrorCode.ADOPTION_DOCUMENT_NOT_FOUND;

import homes.banzzokee.global.error.exception.CustomException;

public class AdoptionDocumentNotFoundException extends CustomException {

  public AdoptionDocumentNotFoundException() {
    super(ADOPTION_DOCUMENT_NOT_FOUND);
  }
}
