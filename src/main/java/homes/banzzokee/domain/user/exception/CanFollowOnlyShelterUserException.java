package homes.banzzokee.domain.user.exception;

import static homes.banzzokee.global.error.ErrorCode.CAN_FOLLOW_ONLY_SHELTER_USER;

import homes.banzzokee.global.error.exception.CustomException;

public class CanFollowOnlyShelterUserException extends CustomException {

  public CanFollowOnlyShelterUserException() {
    super(CAN_FOLLOW_ONLY_SHELTER_USER);
  }
}
