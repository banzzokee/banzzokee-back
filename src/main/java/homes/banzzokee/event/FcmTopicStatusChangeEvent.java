package homes.banzzokee.event;

import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.domain.bookmark.entity.Bookmark;
import homes.banzzokee.domain.user.entity.Follow;
import homes.banzzokee.event.dto.FcmTopicStatusDto;
import homes.banzzokee.event.type.FcmTopicAction;
import homes.banzzokee.infra.rabbitmq.event.BaseMessage;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
public class FcmTopicStatusChangeEvent extends BaseMessage<FcmTopicStatusDto> {

  public static FcmTopicStatusChangeEvent of(FcmTopicAction action, Follow follow) {
    return FcmTopicStatusChangeEvent.builder()
        .routingKey(action.getRoutingKey())
        .payload(FcmTopicStatusDto.of(action, follow))
        .build();
  }

  public static FcmTopicStatusChangeEvent of(FcmTopicAction action, Bookmark bookmark) {
    return FcmTopicStatusChangeEvent.builder()
        .routingKey(action.getRoutingKey())
        .payload(FcmTopicStatusDto.of(action, bookmark))
        .build();
  }
}
