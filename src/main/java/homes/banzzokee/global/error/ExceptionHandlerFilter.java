package homes.banzzokee.global.error;

import static homes.banzzokee.global.error.ErrorCode.ACCESS_TOKEN_EXPIRED;
import static homes.banzzokee.global.error.ErrorCode.INTERNAL_ERROR;
import static homes.banzzokee.global.error.ErrorCode.INVALID_TOKEN;
import static homes.banzzokee.global.error.ErrorCode.NO_AUTHORIZED;
import static homes.banzzokee.global.error.ErrorCode.REFRESH_TOKEN_EXPIRED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import homes.banzzokee.global.security.exception.RefreshTokenExpiredException;
import homes.banzzokee.global.security.exception.TokenInvalidException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response, FilterChain filterChain)
      throws IOException {
    try {
      filterChain.doFilter(request, response);
    } catch (AccessDeniedException e) {
      setErrorResponse(NO_AUTHORIZED, request, response, e);
    } catch (ExpiredJwtException e) {
      setErrorResponse(ACCESS_TOKEN_EXPIRED, request, response, e);
    } catch (TokenInvalidException e) {
      setErrorResponse(INVALID_TOKEN, request, response, e);
    } catch (RefreshTokenExpiredException e) {
      setErrorResponse(REFRESH_TOKEN_EXPIRED, request, response, e);
    } catch (Exception e) {
      setErrorResponse(INTERNAL_ERROR, request, response, e);
    }
  }

  private void setErrorResponse(ErrorCode errorCode, HttpServletRequest request,
      HttpServletResponse response, Exception e) throws IOException {
    log.error("{} is occurred. uri:{}", e.getClass().getName(),
        request.getRequestURI());

    response.setContentType(APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(UTF_8.name());

    response.setStatus(errorCode.getHttpStatus().value());
    objectMapper.writeValue(response.getWriter(), ErrorResponse.of(errorCode));
  }
}
