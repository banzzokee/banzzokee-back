package homes.banzzokee.domain.oauth.controller;

import homes.banzzokee.domain.oauth.dto.TokenDto;
import homes.banzzokee.domain.oauth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  @GetMapping("/google/callback")
  public ResponseEntity<TokenDto> getGoogleToken(@RequestParam("code") String code) {
    return ResponseEntity.ok(authService.getGoogleToken(code));
  }

}