package homes.banzzokee.global.error.exception;

import homes.banzzokee.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class CustomException extends RuntimeException {

  private ErrorCode errorCode;

}
