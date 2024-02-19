package homes.banzzokee.global.security.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import homes.banzzokee.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json; charset=UTF-8");
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    Map<String, String> errorMap = new HashMap<>();
    errorMap.put("error", String.valueOf(ErrorCode.EMAIL_EXIST_DIFFERENT_LOGIN));
    errorMap.put("message", ErrorCode.EMAIL_EXIST_DIFFERENT_LOGIN.getMessage());
    String jsonErrorMessage = objectMapper.writeValueAsString(errorMap);
    response.getWriter().write(jsonErrorMessage);
    log.info("OAuth2FailureHandler: {}", exception.getMessage());
  }
}

