package homes.banzzokee.domain.adoption.exception;

import static homes.banzzokee.global.error.ErrorCode.ALREADY_ADOPTION_IS_FINISHED;

import homes.banzzokee.global.error.exception.CustomException;

public class AlreadyFinishedAdoptionException extends CustomException {

  public AlreadyFinishedAdoptionException() {
    super(ALREADY_ADOPTION_IS_FINISHED);
  }
}
