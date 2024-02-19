package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.global.validator.annotation.BreedValid;
import homes.banzzokee.global.validator.annotation.DogGenderValid;
import homes.banzzokee.global.validator.annotation.DogSizeValid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class AdoptionSearchRequest {

  @Size(max = 3)
  private List<BreedType> breed;

  private DogSize size;

  private Boolean neutering;

  private Boolean healthChecked;

  private DogGender gender;

  private AgeRange ageRange;

  @Getter
  @AllArgsConstructor
  public static class AgeRange {
    @Min(0) @Max(100)
    private final int minAge;
    @Min(0) @Max(100)
    private final int maxAge;
  }
}
