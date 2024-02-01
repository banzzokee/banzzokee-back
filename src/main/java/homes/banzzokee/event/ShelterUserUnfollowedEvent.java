package homes.banzzokee.event;

import static homes.banzzokee.event.type.FcmTopic.SHELTER;
import static homes.banzzokee.event.type.FcmTopicAction.UNSUBSCRIBE;

import homes.banzzokee.domain.user.entity.Follow;
import homes.banzzokee.event.dto.FcmTopicStatusDto;
import lombok.experimental.SuperBuilder;

/**
 * 보호소 사용자 언팔로우 이벤트
 */
@SuperBuilder
public class ShelterUserUnfollowedEvent extends FcmTopicUnsubscribeEvent {

  public static ShelterUserUnfollowedEvent fromEntity(Follow follow) {
    return ShelterUserUnfollowedEvent.builder()
        .payload(FcmTopicStatusDto.builder()
            .action(UNSUBSCRIBE)
            .topic(SHELTER)
            .topicId(follow.getFollower().getShelter().getId())
            .userId(follow.getFollowee().getId())
            .build())
        .build();
  }
}
