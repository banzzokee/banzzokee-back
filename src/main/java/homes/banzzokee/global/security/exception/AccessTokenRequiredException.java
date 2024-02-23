package homes.banzzokee.global.security.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.ACCESS_TOKEN_REQUIRED;

public class AccessTokenRequiredException extends CustomException {

  public AccessTokenRequiredException() {
    super(ACCESS_TOKEN_REQUIRED);
  }
}
