package homes.banzzokee.domain.user.exception;

import static homes.banzzokee.global.error.ErrorCode.ORIGIN_PASSWORD_EQUALS_NEW_PASSWORD;

import homes.banzzokee.global.error.exception.CustomException;

public class OriginPasswordEqualsNewPasswordException extends CustomException {

  public OriginPasswordEqualsNewPasswordException() {
    super(ORIGIN_PASSWORD_EQUALS_NEW_PASSWORD);
  }
}
