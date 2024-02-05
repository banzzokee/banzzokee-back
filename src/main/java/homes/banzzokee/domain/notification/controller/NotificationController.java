package homes.banzzokee.domain.notification.controller;

import homes.banzzokee.domain.notification.dto.FcmTokenRegisterRequest;
import homes.banzzokee.domain.notification.service.NotificationService;
import homes.banzzokee.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @PutMapping("tokens")
  public void registerFcmToken(@Valid @RequestBody FcmTokenRegisterRequest request,
      @RequestHeader("User-Agent") String userAgent,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    notificationService.registerFcmToken(request, userAgent, userDetails.getUserId());
  }
}
