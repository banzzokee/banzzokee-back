package homes.banzzokee.domain.user.service;

import static homes.banzzokee.global.error.ErrorCode.CONFIRM_PASSWORD_UNMATCHED;
import static homes.banzzokee.global.error.ErrorCode.PASSWORD_UNMATCHED;

import homes.banzzokee.domain.type.ImagePath;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.FollowRepository;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.dto.ChangePasswordRequest;
import homes.banzzokee.domain.user.dto.ChangePasswordResponse;
import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.UserProfileUpdateRequest;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.UserProfileUpdateResponse;
import homes.banzzokee.domain.user.dto.WithdrawUserRequest;
import homes.banzzokee.domain.user.dto.WithdrawUserResponse;
import homes.banzzokee.domain.user.entity.Follow;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.CanFollowOnlyShelterUserException;
import homes.banzzokee.domain.user.exception.CanNotFollowSelfException;
import homes.banzzokee.domain.user.exception.OriginPasswordEqualsNewPasswordException;
import homes.banzzokee.domain.user.exception.UserAlreadyWithdrawnException;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.global.error.exception.CustomException;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final FollowRepository followRepository;
  private final FileUploadService s3Service;

  @Transactional(readOnly = true)
  public UserProfileDto getUserProfile(long userId) {
    return UserProfileDto.fromEntity(findByUserIdOrThrow(userId));
  }

  @Transactional
  public FollowDto followUser(long followeeId, long followerId) {
    throwIfFolloweeEqualsFollower(followeeId, followerId);
    User followee = findByUserIdOrThrow(followeeId);
    throwIfFolloweeIsNotShelter(followee);
    User follower = findByUserIdOrThrow(followerId);

    // TODO: 팔로우 알림 발생
    return FollowDto.from(followUserIfNotFollowing(followee, follower));
  }

  @Transactional
  public void unfollowUser(long followeeId, long followerId) {
    unfollowUserIfFollowing(followeeId, followerId);
  }

  private User findByUserIdOrThrow(long userId) {
    return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
  }

  private Follow findFollowOrNull(long followeeId, long followerId) {
    return followRepository
        .findByFolloweeIdAndFollowerId(followeeId, followerId)
        .orElse(null);
  }

  private Follow followUserIfNotFollowing(User followee, User follower) {
    Follow follow = findFollowOrNull(followee.getId(), follower.getId());

    if (follow == null) {
      follow = followRepository.save(Follow.builder()
          .followee(followee)
          .follower(follower)
          .build());
    }

    return follow;
  }

  private void unfollowUserIfFollowing(long followeeId, long followerId) {
    Follow follow = findFollowOrNull(followeeId, followerId);

    if (follow != null) {
      followRepository.delete(follow);
    }
  }

  private void throwIfFolloweeIsNotShelter(User followee) {
    if (!followee.hasShelter()) {
      throw new CanFollowOnlyShelterUserException();
    }
  }

  private void throwIfFolloweeEqualsFollower(long followeeId, long followerId) {
    if (followeeId == followerId) {
      throw new CanNotFollowSelfException();
    }
  }

  @Transactional
  public WithdrawUserResponse withdrawUser(WithdrawUserRequest request, long userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    throwIfAlreadyWithdrawn(user);
    throwIfPasswordUnmatched(user, request.getPassword());
    user.withdraw();
    return WithdrawUserResponse.fromEntity(user);
  }

  private void throwIfAlreadyWithdrawn(User user) {
    if (user.isWithdrawn()) {
      throw new UserAlreadyWithdrawnException();
    }
  }

  private void throwIfPasswordUnmatched(User user, String password) {
    // TODO: Auth 회원가입 기능 완료 후, PasswordEncoder 비교 로직 추가
    if (!user.getPassword().equals(password)) {
      // TODO: Auth에 PasswordUnmatchedException 추가
      throw new CustomException(PASSWORD_UNMATCHED);
    }
  }

  private void throwIfConfirmPasswordUnmatched(String password, String confirmPassword) {
    // TODO: Auth 회원가입 기능 완료 후, PasswordEncoder 비교 로직 추가
    if (!password.equals(confirmPassword)) {
      // TODO: Auth에 ConfirmPasswordUnmatched 추가
      throw new CustomException(CONFIRM_PASSWORD_UNMATCHED);
    }
  }

  @Transactional
  public ChangePasswordResponse changePassword(ChangePasswordRequest request,
      long userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    validateChangePasswordRequest(request, user);
    user.changePassword(request.getNewPassword());
    return ChangePasswordResponse.fromEntity(user);
  }

  private void validateChangePasswordRequest(ChangePasswordRequest request, User user) {
    throwIfAlreadyWithdrawn(user);
    throwIfPasswordUnmatched(user, request.getOriginPassword());
    throwIfOriginPasswordSameNewPassword(request.getOriginPassword(), request.getNewPassword());
    throwIfConfirmPasswordUnmatched(request.getNewPassword(), request.getConfirmPassword());
  }

  private void throwIfOriginPasswordSameNewPassword(String originPassword,
      String newPassword) {
    if (originPassword.equals(newPassword)) {
      throw new OriginPasswordEqualsNewPasswordException();
    }
  }

  /**
   * 사용자 프로필 수정
   *
   * @param request    사용자 프로필 수정 요청
   * @param profileImage 프로필 이미지
   * @param userId     사용자 아이디
   * @return 사용자 프로필 수정 응답
   */
  @Transactional
  public UserProfileUpdateResponse updateUserProfile(UserProfileUpdateRequest request,
      MultipartFile profileImage, long userId) {
    // TODO: userDetails & userId가 일치하는지 확인
    User user = findByUserIdOrThrow(userId);
    S3Object oldProfileImage = user.getProfileImage();

    S3Object uploadedImage = uploadProfileImgIfExists(profileImage);
    user.updateProfile(request.getNickname(), request.getIntroduce(), uploadedImage);
    deleteOldProfileImageIfExists(oldProfileImage);

    return UserProfileUpdateResponse.fromEntity(user);
  }

  private S3Object uploadProfileImgIfExists(MultipartFile profileImage) {
    if (profileImage != null && !profileImage.isEmpty()) {
      return S3Object.from(s3Service.uploadOneFile(profileImage, ImagePath.PROFILE));
    }
    return null;
  }

  private void deleteOldProfileImageIfExists(S3Object oldProfileImage) {
    if (oldProfileImage == null) {
      return;
    }

    try {
      s3Service.deleteFile(oldProfileImage.getFileName());
    } catch (Exception e) {
      // TODO: 삭제 못한 이미지에 대한 예외 처리
      log.error("delete profile image failed. file={}", oldProfileImage, e);
    }
  }
}
