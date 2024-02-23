package homes.banzzokee.global.security.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.ACCESS_TOKEN_IS_BLACKLIST;

public class AccessTokenBlackListedException extends CustomException {

  public AccessTokenBlackListedException() {
    super(ACCESS_TOKEN_IS_BLACKLIST);
  }
}
