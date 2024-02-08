package homes.banzzokee.domain.adoption.exception;

import static homes.banzzokee.global.error.ErrorCode.ADOPTION_IS_DELETED;

import homes.banzzokee.global.error.exception.CustomException;

public class AdoptionIsDeletedException extends CustomException{
  public AdoptionIsDeletedException() {
    super(ADOPTION_IS_DELETED);
  }
}