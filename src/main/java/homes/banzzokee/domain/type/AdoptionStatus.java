package homes.banzzokee.domain.type;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdoptionStatus {

  ADOPTING("분양중"),
  RESERVING("예약중"),
  FINISHED("분양완료");

  private final String status;

  private static final Map<String, AdoptionStatus> adoptionStatusMap =
      Collections.unmodifiableMap(Stream.of(values())
          .collect(Collectors.toMap(AdoptionStatus::getStatus, Function.identity()))
  );

  public static AdoptionStatus findByString(String status) {
    return adoptionStatusMap.get(status);
  }
}
