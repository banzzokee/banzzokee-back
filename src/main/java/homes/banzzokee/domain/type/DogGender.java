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
public enum DogGender {

  MALE("수컷"),
  FEMALE("암컷")
  ;

  private final String gender;

  private static final Map<String, DogGender> dogGenders = Collections.unmodifiableMap(
      Stream.of(values())
          .collect(Collectors.toMap(DogGender::getGender, Function.identity()))
  );

  public static DogGender findByString(String gender) {
    return dogGenders.get(gender);
  }

  public static boolean contains(String gender) {
    return dogGenders.containsKey(gender);
  }
}
