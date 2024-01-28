package homes.banzzokee.domain.user.controller;

import homes.banzzokee.domain.user.dto.ChangePasswordRequest;
import homes.banzzokee.domain.user.dto.ChangePasswordResponse;
import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.UserProfileUpdateRequest;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.UserProfileUpdateResponse;
import homes.banzzokee.domain.user.dto.WithdrawUserRequest;
import homes.banzzokee.domain.user.dto.WithdrawUserResponse;
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
  public WithdrawUserResponse withdrawalUser(
      @Valid @RequestBody WithdrawUserRequest request, @RequestParam long userId) {
    // TODO: userId -> @AuthenticationPrincipal 변경
    return userService.withdrawUser(request, userId);
  }

  @PatchMapping("me/change-password")
  public ChangePasswordResponse changePassword(
      @Valid @RequestBody ChangePasswordRequest request, @RequestParam long userId) {
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
