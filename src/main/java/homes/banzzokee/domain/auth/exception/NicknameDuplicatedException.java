package homes.banzzokee.domain.auth.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.NICKNAME_DUPLICATED;

public class NicknameDuplicatedException extends CustomException {

  public NicknameDuplicatedException() {
    super(NICKNAME_DUPLICATED);
  }
}
