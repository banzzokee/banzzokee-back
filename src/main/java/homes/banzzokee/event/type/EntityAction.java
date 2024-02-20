package homes.banzzokee.event.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntityAction {
  ADOPTION_CREATED("adoption.created"),
  ADOPTION_UPDATED("adoption.updated"),
  ADOPTION_DELETED("adoption.deleted"),
  ADOPTION_STATUS_CHANGED("adoption.status.changed"),

  REVIEW_CREATED("review.created");

  private final String routingKey;
}
