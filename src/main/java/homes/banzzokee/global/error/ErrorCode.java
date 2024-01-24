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
  USER_ALREADY_WITHDRAWN(BAD_REQUEST, "이미 탈퇴한 사용자입니다."),
  PASSWORD_UNMATCHED(BAD_REQUEST, "패스워드가 일치하지 않습니다."),
  ORIGIN_PASSWORD_EQUALS_NEW_PASSWORD(BAD_REQUEST, "기존 패스워드와 새로운 패스워드가 같습니다."),
  CONFIRM_PASSWORD_UNMATCHED(BAD_REQUEST, "재확인 패스워드가 다릅니다."),
  ;

  private final HttpStatus httpStatus;
  private final String message;
}
