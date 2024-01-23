package homes.banzzokee.domain.auth.controller;

import homes.banzzokee.domain.auth.dto.SignupDto;
import homes.banzzokee.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/sign-up")
  public ResponseEntity<Void> signup(@Valid @RequestBody SignupDto signupDto) {
    return ResponseEntity.ok().body(authService.signup(signupDto));
  }

}
