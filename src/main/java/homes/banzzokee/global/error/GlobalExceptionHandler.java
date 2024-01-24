package homes.banzzokee.global.error;

import static homes.banzzokee.global.error.ErrorCode.HTTP_MESSAGE_NOT_READABLE;
import static homes.banzzokee.global.error.ErrorCode.INTERNAL_ERROR;
import static homes.banzzokee.global.error.ErrorCode.JSON_EOF_ERROR;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.core.io.JsonEOFException;
import homes.banzzokee.global.error.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleCustomException(CustomException e,
      HttpServletRequest request) {
    log.error("[CustomException] {} is occurred. uri:{}", e.getErrorCode(),
        request.getRequestURI());

    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(
            ErrorResponse.of(e.getErrorCode())
        );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e, HttpServletRequest request) {
    log.error("MethodArgumentNotValidException is occurred. uri:{}",
        request.getRequestURI());

    return ResponseEntity
        .status(e.getStatusCode())
        .body(ErrorResponse.of(e.getFieldErrors()));
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
      HandlerMethodValidationException e, HttpServletRequest request) {
    log.error("HandlerMethodValidationException is occurred. uri:{}",
        request.getRequestURI());

    return ResponseEntity
        .status(e.getStatusCode())
        .body(ErrorResponse.of(e.getAllValidationResults()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e, HttpServletRequest request) {
    log.error("HttpMessageNotReadableException is occurred. uri:{}",
        request.getRequestURI());

    if (e.getCause() instanceof JsonEOFException) {
      return ResponseEntity
          .status(BAD_REQUEST)
          .body(ErrorResponse.of(JSON_EOF_ERROR));
    }

    return ResponseEntity
        .status(BAD_REQUEST)
        .body(ErrorResponse.of(HTTP_MESSAGE_NOT_READABLE, e.getMessage()));
  }

  @ExceptionHandler(RuntimeException.class)
  private ResponseEntity<ErrorResponse> handleRuntimeException(
      RuntimeException e, HttpServletRequest request) {
    log.error("RuntimeException is occurred. uri:{}",
        request.getRequestURI());

    return ResponseEntity
        .status(INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(INTERNAL_ERROR, e.getMessage()));
  }
}
