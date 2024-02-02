package homes.banzzokee.event;

import homes.banzzokee.event.dto.FcmTopicStatusDto;
import homes.banzzokee.infra.rabbitmq.event.BaseMessage;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class FcmTopicStatusChangeEvent extends BaseMessage<FcmTopicStatusDto> {

  @Override
  public String getRoutingKey() {
    return null;
  }
}
