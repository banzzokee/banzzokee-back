package homes.banzzokee.event.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FcmTopicAction {
  SUBSCRIBE("topic.subscribe"),
  UNSUBSCRIBE("topic.unsubscribe");

  private final String routingKey;
}
