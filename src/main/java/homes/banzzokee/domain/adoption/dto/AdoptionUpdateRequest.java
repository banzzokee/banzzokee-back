package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.global.validator.annotation.DateValid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class AdoptionUpdateRequest {

  @NotBlank @Size(max = 50)
  private final String title;

  @NotBlank @Size(max = 500)
  private final String content;

  @NotNull
  private final BreedType breed;

  @NotNull
  private final DogSize size;

  private final boolean neutering;

  @NotNull
  private final DogGender gender;

  @Max(100)
  private final int age;

  private final boolean healthChecked;

  @NotNull @DateValid
  private final String registeredAt;

}
