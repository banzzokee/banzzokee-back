package homes.banzzokee.domain.user.controller;

import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
