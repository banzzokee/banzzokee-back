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
public enum DogSize implements AdoptionEnum{

  ULTRA_SMALL("초소형"),
  SMALL("소형"),
  MEDIUM("중형"),
  LARGE("대형")
  ;

  private final String value;

  private static final Map<String, DogSize> dogSizes = Collections.unmodifiableMap(
      Stream.of(values())
          .collect(Collectors.toMap(DogSize::getValue, Function.identity()))
  );

  public static DogSize findByString(String size) {
    return dogSizes.get(size);
  }

  public static boolean contains(String size) {
    return dogSizes.containsKey(size);
  }

  @Override
  public String getKey() {
    return name();
  }
}
