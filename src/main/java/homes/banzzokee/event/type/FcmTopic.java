package homes.banzzokee.event.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FcmTopic {
  SHELTER("shelter"),
  ADOPTION("adoption");

  private final String name;
}
