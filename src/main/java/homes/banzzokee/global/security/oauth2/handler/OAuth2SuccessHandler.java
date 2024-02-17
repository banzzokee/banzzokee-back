package homes.banzzokee.global.security.oauth2.handler;

import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.security.jwt.JwtTokenProvider;
import homes.banzzokee.global.util.redis.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  public static final String SUCCESS_URI = "/oauth2/success";
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisService redisService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException {
    log.info("소셜 로그인 성공! 서버 로그를 확인해주세요.");
    UserDetailsImpl oauth2User = (UserDetailsImpl) authentication.getPrincipal();
    String accessToken = jwtTokenProvider.createAccessToken(oauth2User.getUsername());
    boolean isFirstSocialLogin = checkIfFirstSocialLogin(oauth2User.getUsername());
    String redirectUrl;
    if (isFirstSocialLogin) {
      redirectUrl = SUCCESS_URI + "?accessToken=" + accessToken + "&isFirstLogin=true";
      String refreshToken = jwtTokenProvider.createRefreshToken(oauth2User.getUsername());
      redisService.setRefreshToken(
          oauth2User.getUsername(), refreshToken, jwtTokenProvider.getRefreshTokenExpire());
    } else {
      redirectUrl = SUCCESS_URI + "?accessToken=" + accessToken + "&isFirstLogin=false";
    }
    getRedirectStrategy().sendRedirect(request, response, redirectUrl);
  }

  /**
   * 최초 소셜로그인 레디스 확인
   */
  private boolean checkIfFirstSocialLogin(String email) {
    return !redisService.hasUserEmail(email);
  }
}