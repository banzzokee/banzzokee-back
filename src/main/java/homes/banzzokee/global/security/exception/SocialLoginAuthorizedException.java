package homes.banzzokee.global.security.exception;

import homes.banzzokee.global.error.exception.CustomException;

import static homes.banzzokee.global.error.ErrorCode.NO_SOCIAL_LOGIN_AUTHORIZED;

public class SocialLoginAuthorizedException extends CustomException {

  public SocialLoginAuthorizedException() {
    super(NO_SOCIAL_LOGIN_AUTHORIZED);
  }
}
