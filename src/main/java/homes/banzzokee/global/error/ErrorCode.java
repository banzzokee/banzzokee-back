package homes.banzzokee.global.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

  FAILED(BAD_REQUEST, "실패했습니다."),

  /**
   * 이미지 관련
   */
  FAIL_TO_UPLOAD_FILE(INTERNAL_SERVER_ERROR, "이미지 업로드에 실패하였습니다.")

  ;

  private final HttpStatus httpStatus;
  private final String message;
}
