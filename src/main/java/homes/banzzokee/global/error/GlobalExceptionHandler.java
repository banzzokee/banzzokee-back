package homes.banzzokee.global.error;

import homes.banzzokee.global.config.stomp.exception.SocketException;
import homes.banzzokee.global.error.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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

  @MessageExceptionHandler  // 메세지 전송 에러 핸들러
  @SendToUser("/queue/error") // 특정 유저에게 메세지 전송 ->
  // "/user/queue/error" 구독한 유저
  public ResponseEntity<ErrorResponse> handleException(
      Principal principal,  // 쓰이지 않더라도 파라미터로 받아와야 특정한 유저에게 보낼 수 있음
      SocketException e) {

    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(
            ErrorResponse.of(e.getErrorCode())
        );
  }

}
