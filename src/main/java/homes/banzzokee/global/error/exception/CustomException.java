package homes.banzzokee.global.error.exception;

import homes.banzzokee.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {

  private final ErrorCode errorCode;

}
