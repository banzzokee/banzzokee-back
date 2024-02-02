package homes.banzzokee.event;

import static lombok.AccessLevel.PROTECTED;

import homes.banzzokee.event.dto.FcmTopicStatusDto;
import homes.banzzokee.infra.rabbitmq.event.BaseMessage;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
public class FcmTopicStatusChangeEvent extends BaseMessage<FcmTopicStatusDto> {

  @Override
  public String getRoutingKey() {
    return null;
  }
}
