package homes.banzzokee.domain.auth.controller;

import homes.banzzokee.domain.auth.dto.EmailDto;
import homes.banzzokee.domain.auth.dto.EmailVerifyDto;
import homes.banzzokee.domain.auth.dto.SignupDto;
import homes.banzzokee.domain.auth.service.AuthService;
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

  @PostMapping("/sign-up")
  public ResponseEntity<Void> signup(@Valid @RequestBody SignupDto signupDto) {
    authService.signup(signupDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/send-verify")
  public ResponseEntity<?> sendVerificationCode(@Valid @RequestBody EmailDto emailDto) {
    authService.sendVerificationCode(emailDto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/verify")
  public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailVerifyDto emailVerifyDto) {
    authService.verifyEmail(emailVerifyDto);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/nickname-check")
  public ResponseEntity<?> checkNickname(
      @Length(max = 10, message = "닉네임은 최대 10자리까지 가능합니다.") @RequestParam String nickname) {
    return ResponseEntity.ok(authService.checkNickname(nickname));
  }
}