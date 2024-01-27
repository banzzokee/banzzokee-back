package homes.banzzokee.domain.shelter.exception;

import static homes.banzzokee.global.error.ErrorCode.USER_ALREADY_REGISTER_SHELTER;

import homes.banzzokee.global.error.exception.CustomException;
import lombok.Getter;

@Getter
public class UserAlreadyRegisterShelterException extends CustomException {

  private final Long userId;
  private final Long shelterId;

  public UserAlreadyRegisterShelterException(Long userId, Long shelterId) {
    super(USER_ALREADY_REGISTER_SHELTER);
    this.userId = userId;
    this.shelterId = shelterId;
  }
}
