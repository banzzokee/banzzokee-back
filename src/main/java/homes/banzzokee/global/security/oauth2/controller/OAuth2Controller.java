package homes.banzzokee.global.security.oauth2.controller;

import homes.banzzokee.domain.auth.dto.TokenResponse;
import homes.banzzokee.global.security.jwt.JwtTokenProvider;
import homes.banzzokee.global.security.oauth2.dto.NicknameRequest;
import homes.banzzokee.global.security.oauth2.dto.OAuth2Response;
import homes.banzzokee.global.security.oauth2.service.Oauth2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2")
public class OAuth2Controller {

  private final Oauth2Service oauth2Service;

  @PostMapping("/sign-up")
  public ResponseEntity<TokenResponse> signup(@RequestHeader("Authorization") String token,
                                              @Valid @RequestBody NicknameRequest nicknameRequest) {
    return ResponseEntity.ok(oauth2Service.signup(token,nicknameRequest));
  }

  @GetMapping("/success")
  public OAuth2Response oAuth2LoginSuccess(HttpServletRequest request) {
    return OAuth2Response.builder()
        .accessToken(request.getSession().getAttribute("accessToken").toString())
        .refreshToken(request.getSession().getAttribute("refreshToken").toString())
        .firstLogin((Boolean) request.getSession().getAttribute("isFirstLogin"))
        .build();
  }
}
