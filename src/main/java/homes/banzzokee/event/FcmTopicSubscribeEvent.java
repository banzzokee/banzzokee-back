package homes.banzzokee.event;

import lombok.experimental.SuperBuilder;

/**
 * FCM 토픽 구독 이벤트
 */
@SuperBuilder
public abstract class FcmTopicSubscribeEvent extends FcmTopicStatusChangeEvent {

  @Override
  public String getRoutingKey() {
    return "topic.subscribe";
  }
}
