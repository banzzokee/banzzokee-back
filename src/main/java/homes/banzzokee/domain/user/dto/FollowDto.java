package homes.banzzokee.domain.user.dto;

import homes.banzzokee.domain.user.entity.Follow;
import homes.banzzokee.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class FollowDto {

  private final FollowUserDto follower;

  private final FollowUserDto followee;

  @Builder
  @Getter
  @AllArgsConstructor
  public static class FollowUserDto {

    private final Long userId;

    private final String nickname;

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
