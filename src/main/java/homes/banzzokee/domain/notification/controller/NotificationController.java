package homes.banzzokee.domain.notification.controller;

import homes.banzzokee.domain.notification.dto.FcmTokenRegisterRequest;
import homes.banzzokee.domain.notification.dto.NotificationDto;
import homes.banzzokee.domain.notification.service.NotificationService;
import homes.banzzokee.global.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

  @GetMapping
  public Slice<NotificationDto> getNotificationList(@RequestParam int page,
      @RequestParam int size,
      @RequestParam(required = false) Boolean checked,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return notificationService.getNotificationList(
        PageRequest.of(page, size),
        checked,
        userDetails.getUserId());
  }

  @PostMapping("{notificationId}/check")
  public void checkNotification(@PathVariable Long notificationId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    notificationService.checkNotification(notificationId, userDetails.getUserId());
  }

  @PostMapping("check-all")
  public void checkAllNotifications(
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    notificationService.checkAllNotifications(userDetails.getUserId());
  }
}
