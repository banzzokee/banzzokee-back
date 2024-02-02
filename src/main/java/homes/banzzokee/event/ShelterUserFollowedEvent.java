package homes.banzzokee.event;

import static homes.banzzokee.event.type.FcmTopicCategory.SHELTER;
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
            .topicCategory(SHELTER)
            .topicId(follow.getFollowee().getShelter().getId())
            .userId(follow.getFollower().getId())
            .build())
        .build();
  }
}
