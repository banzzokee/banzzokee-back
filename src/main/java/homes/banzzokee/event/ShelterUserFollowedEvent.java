package homes.banzzokee.event;

import static homes.banzzokee.event.type.FcmTopic.SHELTER;
import static homes.banzzokee.event.type.FcmTopicAction.SUBSCRIBE;

import homes.banzzokee.domain.user.entity.Follow;
import homes.banzzokee.event.dto.FcmTopicStatusDto;
import lombok.experimental.SuperBuilder;

/**
 * 보호소 사용자 팔로우 이벤트
 */
@SuperBuilder
public class ShelterUserFollowedEvent extends FcmTopicSubscribeEvent {

  public static ShelterUserFollowedEvent fromEntity(Follow follow) {
    return ShelterUserFollowedEvent.builder()
        .payload(FcmTopicStatusDto.builder()
            .action(SUBSCRIBE)
            .topic(SHELTER)
            .topicId(follow.getFollower().getShelter().getId())
            .userId(follow.getFollowee().getId())
            .build())
        .build();
  }
}
