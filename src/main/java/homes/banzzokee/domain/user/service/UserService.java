package homes.banzzokee.domain.user.service;

import static homes.banzzokee.event.type.FcmTopicAction.SUBSCRIBE;
import static homes.banzzokee.event.type.FcmTopicAction.UNSUBSCRIBE;

import homes.banzzokee.domain.auth.exception.ConfirmPasswordUnMatchException;
import homes.banzzokee.domain.auth.exception.PasswordUnmatchedException;
import homes.banzzokee.domain.type.FilePath;
import homes.banzzokee.domain.type.S3Object;
import homes.banzzokee.domain.user.dao.FollowRepository;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.PasswordChangeRequest;
import homes.banzzokee.domain.user.dto.PasswordChangeResponse;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.dto.UserProfileUpdateRequest;
import homes.banzzokee.domain.user.dto.UserProfileUpdateResponse;
import homes.banzzokee.domain.user.dto.UserWithdrawRequest;
import homes.banzzokee.domain.user.dto.UserWithdrawResponse;
import homes.banzzokee.domain.user.entity.Follow;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.CanFollowOnlyShelterUserException;
import homes.banzzokee.domain.user.exception.CanNotFollowSelfException;
import homes.banzzokee.domain.user.exception.OriginPasswordEqualsNewPasswordException;
import homes.banzzokee.domain.user.exception.UserAlreadyWithdrawnException;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import homes.banzzokee.event.FcmTopicStatusChangeEvent;
import homes.banzzokee.infra.fileupload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final ApplicationEventPublisher eventPublisher;
  private final PasswordEncoder passwordEncoder;

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
      eventPublisher.publishEvent(FcmTopicStatusChangeEvent.of(SUBSCRIBE, follow));
    }

    return follow;
  }

  private void unfollowUserIfFollowing(long followeeId, long followerId) {
    Follow follow = findFollowOrNull(followeeId, followerId);

    if (follow != null) {
      followRepository.delete(follow);
      eventPublisher.publishEvent(FcmTopicStatusChangeEvent.of(UNSUBSCRIBE, follow));
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
  public UserWithdrawResponse withdrawUser(UserWithdrawRequest request, long userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    throwIfAlreadyWithdrawn(user);
    throwIfPasswordUnmatched(user, request.getPassword());
    user.withdraw();
    return UserWithdrawResponse.fromEntity(user);
  }

  private void throwIfAlreadyWithdrawn(User user) {
    if (user.isWithdrawn()) {
      throw new UserAlreadyWithdrawnException();
    }
  }

  private void throwIfPasswordUnmatched(User user, String password) {
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new PasswordUnmatchedException();
    }
  }

  private void throwIfConfirmPasswordUnmatched(String password, String confirmPassword) {
    if (!password.equals(confirmPassword)) {
      throw new ConfirmPasswordUnMatchException();
    }
  }

  @Transactional
  public PasswordChangeResponse changePassword(PasswordChangeRequest request,
      long userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    validateChangePasswordRequest(request, user);
    user.changePassword(request.getNewPassword());
    return PasswordChangeResponse.fromEntity(user);
  }

  private void validateChangePasswordRequest(PasswordChangeRequest request, User user) {
    throwIfAlreadyWithdrawn(user);
    throwIfPasswordUnmatched(user, request.getOriginPassword());
    throwIfOriginPasswordSameNewPassword(request.getOriginPassword(),
        request.getNewPassword());
    throwIfConfirmPasswordUnmatched(request.getNewPassword(),
        request.getConfirmPassword());
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
   * @param request      사용자 프로필 수정 요청
   * @param profileImage 프로필 이미지
   * @param userId       사용자 아이디
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
      return S3Object.from(s3Service.uploadOneFile(profileImage, FilePath.PROFILE));
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
