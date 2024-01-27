package homes.banzzokee.domain.room.dto;

import homes.banzzokee.domain.user.entity.User;
import lombok.Builder;

/**
 * 채팅 유저
 *
 * @param userId
 * @param profileImgUrl
 * @param nickname
 */
@Builder
public record ChatUserDto(
    Long userId,
    String profileImgUrl,
    String nickname
) {

  public static ChatUserDto fromEntity(User user) {
    return ChatUserDto.builder()
        .userId(user.getId())
        .profileImgUrl(user.getProfileImageUrl())
        .nickname(user.getNickname())
        .build();
  }

}
