package homes.banzzokee.domain.auth.controller;

import homes.banzzokee.domain.auth.dto.EmailDto;
import homes.banzzokee.domain.auth.dto.SignupDto;
import homes.banzzokee.domain.auth.dto.EmailVerifyDto;
import homes.banzzokee.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

//  @PostMapping("/sign-up")
//  public ResponseEntity<Void> signup(@Valid @RequestBody SignupDto signupDto) {
//    return ResponseEntity.ok().body(authService.signup(signupDto));
//  }

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
}