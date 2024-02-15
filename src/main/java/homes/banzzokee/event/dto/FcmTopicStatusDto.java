package homes.banzzokee.event.dto;

import static homes.banzzokee.event.type.FcmTopicCategory.ADOPTION;
import static homes.banzzokee.event.type.FcmTopicCategory.SHELTER;

import homes.banzzokee.domain.bookmark.entity.Bookmark;
import homes.banzzokee.domain.user.entity.Follow;
import homes.banzzokee.event.type.FcmTopicAction;
import homes.banzzokee.event.type.FcmTopicCategory;
import lombok.Builder;
import lombok.Getter;

/**
 * 토픽 상태 DTO
 */
@Getter
@Builder
public class FcmTopicStatusDto {

  /**
   * 토픽 액션
   */
  private final FcmTopicAction action;

  /**
   * 토픽
   */
  private final FcmTopicCategory topicCategory;

  /**
   * 토픽 객체 아이디
   */
  private final Long topicId;

  /**
   * 사용자 아이디
   */
  private final Long userId;

  public String getTopic() {
    return "topic." + topicCategory.getName() + "." + topicId;
  }

  public static FcmTopicStatusDto of(FcmTopicAction action, Follow follow) {
    return FcmTopicStatusDto.builder()
        .action(action)
        .topicCategory(SHELTER)
        .topicId(follow.getFollowee().getShelter().getId())
        .userId(follow.getFollower().getId())
        .build();
  }

  public static FcmTopicStatusDto of(FcmTopicAction action, Bookmark bookmark) {
    return FcmTopicStatusDto.builder()
        .action(action)
        .topicCategory(ADOPTION)
        .topicId(bookmark.getAdoption().getId())
        .userId(bookmark.getUser().getId())
        .build();
  }
}
