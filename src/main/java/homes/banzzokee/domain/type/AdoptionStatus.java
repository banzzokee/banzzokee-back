package homes.banzzokee.domain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = Shape.OBJECT)
public enum AdoptionStatus implements AdoptionEnum {

  ADOPTING("분양중"),
  RESERVING("예약중"),
  FINISHED("분양완료");

  private final String value;


  private static final Map<String, AdoptionStatus> adoptionStatusMap =
      Collections.unmodifiableMap(Stream.of(values())
          .collect(Collectors.toMap(AdoptionStatus::getValue, Function.identity())));

  public static AdoptionStatus findByString(String status) {
    return adoptionStatusMap.get(status);
  }

  @Override
  public String getKey() {
    return name();
  }
}
