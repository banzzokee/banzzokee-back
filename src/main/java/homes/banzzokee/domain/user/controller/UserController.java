package homes.banzzokee.domain.user.controller;

import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.WithdrawUserRequest;
import homes.banzzokee.domain.user.dto.WithdrawUserResponse;
import homes.banzzokee.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @GetMapping("{userId}")
  public UserProfileDto getUserProfile(@PathVariable long userId) {
    return userService.getUserProfile(userId);
  }

  @PostMapping("me/withdraw")
  public WithdrawUserResponse withdrawalUser(
      @Valid @RequestBody WithdrawUserRequest request, @RequestParam long userId) {
    // TODO: userId -> @AuthenticationPrincipal 변경
    return userService.withdrawUser(request, userId);
  }
}
