package homes.banzzokee.global.error.exception;

import static homes.banzzokee.global.error.ErrorCode.NO_AUTHORIZED;

public class NoAuthorizedException extends CustomException {

  public NoAuthorizedException() {
    super(NO_AUTHORIZED);
  }
}
