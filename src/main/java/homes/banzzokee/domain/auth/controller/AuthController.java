package homes.banzzokee.domain.auth.controller;

import homes.banzzokee.domain.auth.dto.*;
import homes.banzzokee.domain.auth.service.AuthService;
import homes.banzzokee.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/sign-up")
  public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {
    authService.signup(signupRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/send-verify")
  public ResponseEntity<Void> sendVerificationCode(@Valid @RequestBody EmailRequest emailRequest) {
    authService.sendVerificationCode(emailRequest);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/verify")
  public ResponseEntity<Void> verifyEmail(@Valid @RequestBody EmailVerifyRequest emailVerifyRequest) {
    authService.verifyEmail(emailVerifyRequest);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/nickname-check")
  public ResponseEntity<Boolean> checkNickname(
      @Length(max = 10, message = "닉네임은 최대 10자리까지 가능합니다.") @RequestParam String nickname) {
    return ResponseEntity.ok(authService.checkNickname(nickname));
  }

  @PostMapping("/sign-in")
  public ResponseEntity<TokenResponse> signIn(@Valid @RequestBody SignInRequest signInRequest) {
    return ResponseEntity.ok(authService.signIn(signInRequest));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    authService.logout(request.getHeader("Authorization"));
    return ResponseEntity.ok().build();
  }

  @PostMapping("/token/reissue")
  public ResponseEntity<TokenResponse> reissueAccessToken(HttpServletRequest request) {
    return ResponseEntity.ok(authService.reissueAccessToken(request.getHeader("Authorization")));
  }
}