package homes.banzzokee.domain.user.service;

import homes.banzzokee.domain.user.dao.FollowRepository;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.dto.FollowDto;
import homes.banzzokee.domain.user.dto.UserProfileDto;
import homes.banzzokee.domain.user.entity.Follow;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.domain.user.exception.CanFollowOnlyShelterUserException;
import homes.banzzokee.domain.user.exception.CanNotFollowSelfException;
import homes.banzzokee.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final FollowRepository followRepository;

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
}
