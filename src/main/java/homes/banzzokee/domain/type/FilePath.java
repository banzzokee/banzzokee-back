package homes.banzzokee.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilePath {

  PROFILE("profile"),
  ADOPTION("adoption"),
  SHELTER("shelter"),
  REVIEW("review"),
  CHAT("chat")
  ;

  private final String path;
}
