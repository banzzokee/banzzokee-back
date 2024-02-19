package homes.banzzokee.global.error;

import static homes.banzzokee.global.error.ErrorCode.NO_AUTHORIZED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {

    log.error("[handle] 접근 제한 처리");
    setErrorResponse(NO_AUTHORIZED, request, response);
  }

  private void setErrorResponse(ErrorCode errorCode, HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    log.error("{} is occurred. uri:{}", errorCode.name(),
        request.getRequestURI());

    response.setContentType(APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(UTF_8.name());

    response.setStatus(errorCode.getHttpStatus().value());
    objectMapper.writeValue(response.getWriter(), ErrorResponse.of(errorCode));
  }
}
