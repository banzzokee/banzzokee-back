package homes.banzzokee.global.security.jwt;

import static homes.banzzokee.global.error.ErrorCode.ACCESS_TOKEN_EXPIRED;
import static homes.banzzokee.global.error.ErrorCode.INTERNAL_ERROR;
import static homes.banzzokee.global.error.ErrorCode.INVALID_TOKEN;
import static homes.banzzokee.global.error.ErrorCode.NO_AUTHORIZED;
import static homes.banzzokee.global.error.ErrorCode.REFRESH_TOKEN_EXPIRED;

import homes.banzzokee.global.security.UserDetailsServiceImpl;
import homes.banzzokee.global.security.exception.RefreshTokenExpiredException;
import homes.banzzokee.global.security.exception.TokenInvalidException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import static homes.banzzokee.global.security.jwt.JwtTokenProvider.BEARER_LENGTH;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsServiceImpl userDetailsService;

  /**
   * 요청에서 토큰을 추출하고 유효성을 확인한 후, 해당 토큰과 연결된 사용자를 인증합니다.
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String token = resolveToken(request);

    if (token != null) {
      try {
        getAuthenticate(token);

      } catch (ExpiredJwtException e) {
        request.setAttribute("exception", ACCESS_TOKEN_EXPIRED);

      } catch (TokenInvalidException e) {
        request.setAttribute("exception", INVALID_TOKEN);

      } catch (RefreshTokenExpiredException e) {
        request.setAttribute("exception", REFRESH_TOKEN_EXPIRED);

      } catch (AccessDeniedException e) {
        request.setAttribute("exception", NO_AUTHORIZED);

      } catch (Exception e) {
        request.setAttribute("exception", INTERNAL_ERROR);
      }
    }

    filterChain.doFilter(request, response);
  }

  /**
   * HttpServlet Request 에서 토큰을 추출하는 메소드입니다. "Authorization" 헤더를 확인하고, "Bearer"로 시작하는 토큰
   * 값을 반환합니다.
   */
  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
      return bearerToken.substring(BEARER_LENGTH);
    }
    return null;
  }

  /**
   * 주어진 토큰을 이용해 사용자를 인증하는 메소드입니다. 토큰의 유효성을 검사하고, 토큰에 연결된 사용자의 정보를 불러와 인증합니다.
   */
  private void getAuthenticate(String token) {
    jwtTokenProvider.validateToken(token);
    String userEmail = jwtTokenProvider.getUserEmailFromToken(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
    Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
