package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.user.entity.Follow;
import homes.banzzokee.domain.user.entity.User;
import lombok.Builder;

@Builder
public record FollowDto(
    FollowUserDto follower,
    FollowUserDto followee
) {

  @Builder
  public record FollowUserDto(long userId, String nickname) {

    public static FollowUserDto from(User user) {
      return FollowUserDto.builder()
          .userId(user.getId())
          .nickname(user.getNickname())
          .build();
    }
  }

  public static FollowDto from(Follow follow) {
    return FollowDto.builder()
        .follower(FollowUserDto.from(follow.getFollower()))
        .followee(FollowUserDto.from(follow.getFollowee()))
        .build();
  }
}
