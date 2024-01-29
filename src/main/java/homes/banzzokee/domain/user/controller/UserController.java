package homes.banzzokee.domain.user.controller;

import homes.banzzokee.domain.user.dto.PasswordChangeRequest;
import homes.banzzokee.domain.user.dto.PasswordChangeResponse;
import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.UserProfileUpdateRequest;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.UserProfileUpdateResponse;
import homes.banzzokee.domain.user.dto.UserWithdrawRequest;
import homes.banzzokee.domain.user.dto.UserWithdrawResponse;
import homes.banzzokee.domain.user.service.UserService;
import homes.banzzokee.global.validator.annotation.ImageFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
  public UserWithdrawResponse withdrawalUser(
      @Valid @RequestBody UserWithdrawRequest request, @RequestParam long userId) {
    // TODO: userId -> @AuthenticationPrincipal 변경
    return userService.withdrawUser(request, userId);
  }

  @PatchMapping("me/change-password")
  public PasswordChangeResponse changePassword(
      @Valid @RequestBody PasswordChangeRequest request, @RequestParam long userId) {
    // TODO: userId -> @AuthenticationPrincipal 변경
    return userService.changePassword(request, userId);
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

  @PatchMapping(value = "me")
  public UserProfileUpdateResponse updateUserProfile(
      @Valid @RequestPart UserProfileUpdateRequest request,
      @ImageFile() MultipartFile profileImg, @RequestParam long userId) {
    // TODO: userId -> @AuthenticationPrincipal로 바꾸기
    return userService.updateUserProfile(request, profileImg, userId);
  }
}
