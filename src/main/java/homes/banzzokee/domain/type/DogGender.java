package homes.banzzokee.domain.type;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = Shape.OBJECT)
public enum DogGender implements AdoptionEnum{

  MALE("수컷"),
  FEMALE("암컷")
  ;

  private final String value;

  private static final Map<String, DogGender> dogGenders = Collections.unmodifiableMap(
      Stream.of(values())
          .collect(Collectors.toMap(DogGender::getValue, Function.identity()))
  );

  public static DogGender findByString(String gender) {
    return dogGenders.get(gender);
  }

  public static boolean contains(String gender) {
    return dogGenders.containsKey(gender);
  }

  @Override
  public String getKey() {
    return name();
  }
}
