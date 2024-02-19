package homes.banzzokee.global.error;

import static homes.banzzokee.global.error.ErrorCode.ACCESS_TOKEN_EXPIRED;
import static homes.banzzokee.global.error.ErrorCode.INTERNAL_ERROR;
import static homes.banzzokee.global.error.ErrorCode.INVALID_TOKEN;
import static homes.banzzokee.global.error.ErrorCode.NO_AUTHORIZED;
import static homes.banzzokee.global.error.ErrorCode.REFRESH_TOKEN_EXPIRED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException {

    // Filter 로 부터 넘어온 request 에서 getAttribute()로 발생한 예외 추출
    Object exception = request.getAttribute("exception");

    if (exception == NO_AUTHORIZED) {
      setErrorResponse(NO_AUTHORIZED, request, response);

    } else if (exception == ACCESS_TOKEN_EXPIRED) {
      setErrorResponse(ACCESS_TOKEN_EXPIRED, request, response);

    } else if (exception == INVALID_TOKEN) {
      setErrorResponse(INVALID_TOKEN, request, response);

    } else if (exception == REFRESH_TOKEN_EXPIRED) {
      setErrorResponse(REFRESH_TOKEN_EXPIRED, request, response);

    } else if (exception == INTERNAL_ERROR) {
      setErrorResponse(INTERNAL_ERROR, request, response);
    }

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
