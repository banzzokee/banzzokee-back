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


  // chat & room
  ROOM_NOT_FOUND(NOT_FOUND, "채팅방을 찾을 수 없습니다."),
  ;

  private final HttpStatus httpStatus;
  private final String message;
}
