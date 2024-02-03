package homes.banzzokee.event;

import static homes.banzzokee.event.type.FcmTopicAction.SUBSCRIBE;
import static lombok.AccessLevel.PROTECTED;

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
    String routingKey = (action == SUBSCRIBE) ? "topic.subscribe" : "topic.unsubscribe";
    return FcmTopicStatusChangeEvent.builder()
        .routingKey(routingKey)
        .payload(FcmTopicStatusDto.of(action, follow))
        .build();
  }
}
