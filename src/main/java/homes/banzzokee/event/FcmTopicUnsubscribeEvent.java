package homes.banzzokee.event;

import lombok.experimental.SuperBuilder;

/**
 * FCM 토픽 구독 해지 이벤트
 */
@SuperBuilder
public abstract class FcmTopicUnsubscribeEvent extends FcmTopicStatusChangeEvent {

  @Override
  public String getRoutingKey() {
    return "topic.unsubscribe";
  }
}
