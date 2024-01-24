package homes.banzzokee.global.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  FAILED(BAD_REQUEST, "실패했습니다."),
  USER_NOT_FOUND(NOT_FOUND, "사용자를 찾을 수 없습니다."),
  EMAIL_CODE_UNMATCHED(BAD_REQUEST, "인증 코드가 일치하지 않습니다."),
  EMAIL_CODE_INVALID(BAD_REQUEST, "인증 코드가 유효하지 않습니다."),

  ;

  private final HttpStatus httpStatus;
  private final String message;
}
