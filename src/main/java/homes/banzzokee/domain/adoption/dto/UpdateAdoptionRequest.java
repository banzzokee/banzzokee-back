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
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
class UpdateAdoptionRequest {

  @NotBlank
  @Size(max = 50)
  private String title;

  @NotBlank
  @Size(max = 500)
  private String content;

  @NotNull
  @BreedValid
  private String breed;

  @NotNull
  @DogSizeValid
  private String size;

  private boolean neutering;

  @NotNull
  @DogGenderValid
  private String gender;

  @Max(100)
  private int age;

  private boolean healthChecked;

  @NotNull
  @DateValid
  private String registeredAt;

}
