package homes.banzzokee.domain.room.dto;

import homes.banzzokee.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅 유저
 */
@Builder
@Getter
@AllArgsConstructor
public class ChatUserDto {

  private final Long userId;

  private final String profileImgUrl;

  private final String nickname;

  public static ChatUserDto fromEntity(User user) {
    return ChatUserDto.builder()
        .userId(user.getId())
        .profileImgUrl(user.getProfileImageUrl())
        .nickname(user.getNickname())
        .build();
  }

}
