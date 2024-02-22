package homes.banzzokee.domain.user.controller;

import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.FollowDto.FollowUserDto;
import homes.banzzokee.domain.user.dto.PasswordChangeRequest;
import homes.banzzokee.domain.user.dto.PasswordChangeResponse;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.UserProfileUpdateRequest;
import homes.banzzokee.domain.user.dto.UserProfileUpdateResponse;
import homes.banzzokee.domain.user.dto.UserWithdrawRequest;
import homes.banzzokee.domain.user.dto.UserWithdrawResponse;
import homes.banzzokee.domain.user.service.UserService;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.validator.annotation.ImageFile;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
  public UserProfileDto getUserProfile(@PathVariable long userId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return userService.getUserProfile(userId, userDetails.getUserId());
  }

  @GetMapping("me")
  public UserProfileDto getMyProfile(
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return userService.getUserProfile(userDetails.getUserId(), null);
  }

  @PostMapping("me/withdraw")
  public UserWithdrawResponse withdrawalUser(
      @Valid @RequestBody UserWithdrawRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return userService.withdrawUser(request, userDetails.getUserId());
  }

  @PatchMapping("me/change-password")
  public PasswordChangeResponse changePassword(
      @Valid @RequestBody PasswordChangeRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return userService.changePassword(request, userDetails.getUserId());
  }

  @PostMapping("{userId}/follow")
  public FollowDto followUser(@PathVariable long userId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return userService.followUser(userId, userDetails.getUserId());
  }

  @PostMapping("{userId}/unfollow")
  public void unfollowUser(@PathVariable long userId,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    userService.unfollowUser(userId, userDetails.getUserId());
  }

  @PatchMapping(value = "me")
  public UserProfileUpdateResponse updateUserProfile(
      @Valid @RequestPart UserProfileUpdateRequest request,
      @ImageFile() MultipartFile profileImg,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return userService.updateUserProfile(request, profileImg,
        userDetails.getUserId());
  }

  @GetMapping("/me/followers")
  public Slice<FollowUserDto> getMyFollowers(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "10") int size,
      @RequestParam(required = false, defaultValue = "desc") String direction) {
    PageRequest pageRequest = PageRequest.of(page, size,
        Sort.by(Direction.fromString(direction), "createdAt"));
    return userService.getMyFollowers(userDetails.getUserId(), pageRequest);
  }
}
