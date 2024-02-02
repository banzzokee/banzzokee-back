package homes.banzzokee.event;

import static homes.banzzokee.event.type.FcmTopicCategory.SHELTER;
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
            .topicCategory(SHELTER)
            .topicId(follow.getFollowee().getShelter().getId())
            .userId(follow.getFollower().getId())
            .build())
        .build();
  }
}
