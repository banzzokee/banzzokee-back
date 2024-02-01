package homes.banzzokee.event.dto;

import homes.banzzokee.event.type.FcmTopic;
import homes.banzzokee.event.type.FcmTopicAction;
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
  private final FcmTopic topic;

  /**
   * 토픽 객체 아이디
   */
  private final Long topicId;

  /**
   * 사용자 아이디
   */
  private final Long userId;

  public String getTopic() {
    return "topic." + topic.getName() + "." + topicId;
  }
}
