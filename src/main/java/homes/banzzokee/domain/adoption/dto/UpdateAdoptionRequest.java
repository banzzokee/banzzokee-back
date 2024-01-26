package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.global.validator.annotation.BreedValid;
import homes.banzzokee.global.validator.annotation.DateValid;
import homes.banzzokee.global.validator.annotation.DogGenderValid;
import homes.banzzokee.global.validator.annotation.DogSizeValid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UpdateAdoptionRequest {

  @NotBlank @Size(max = 50)
  private final String title;

  @NotBlank @Size(max = 500)
  private final String content;

  @NotNull @BreedValid
  private final String breed;

  @NotNull @DogSizeValid
  private final String size;

  @NotNull
  private final boolean neutering;

  @NotNull @DogGenderValid
  private final String gender;

  @NotNull @Max(100)
  private final int age;

  @NotNull
  private final boolean healthChecked;

  @NotNull @DateValid
  private final String registeredAt;

}
