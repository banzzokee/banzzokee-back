package homes.banzzokee.domain.room.exception;

import static homes.banzzokee.global.error.ErrorCode.ADOPTION_WRITER;

import homes.banzzokee.global.error.exception.CustomException;

public class AdoptionWriterException extends CustomException {

  public AdoptionWriterException() {
    super(ADOPTION_WRITER);
  }
}
