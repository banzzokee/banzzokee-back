package homes.banzzokee.domain.user.controller;

import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

  @PostMapping("{userId}/follow")
  public FollowDto followUser(@PathVariable long userId, @RequestParam long followerId) {
    // TODO: followerId -> @AuthenticationPrincipal로 바꾸기
    return userService.followUser(userId, followerId);
  }

  @PostMapping("{userId}/unfollow")
  public void unfollowUser(@PathVariable long userId, @RequestParam long followerId) {
    // TODO: followerId -> @AuthenticationPrincipal로 바꾸기
    userService.unfollowUser(userId, followerId);
  }
}
