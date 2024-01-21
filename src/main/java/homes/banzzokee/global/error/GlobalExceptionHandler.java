package homes.banzzokee.global.error;

import homes.banzzokee.global.error.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse<?>> handleCustomException(CustomException e, HttpServletRequest request) {
    log.error("[CustomException] {} is occurred. uri:{}", e, request.getRequestURI());

    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(
            ErrorResponse.fail(e.getErrorCode())
        );
  }

}
