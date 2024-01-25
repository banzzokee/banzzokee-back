package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.global.validator.annotation.BreedValid;
import homes.banzzokee.global.validator.annotation.DateValid;
import homes.banzzokee.global.validator.annotation.DogGenderValid;
import homes.banzzokee.global.validator.annotation.DogSizeValid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAdoptionRequest(
    @NotBlank @Size(max = 50) String title,
    @NotBlank @Size(max = 500) String content,
    @NotNull @BreedValid String breed,
    @NotNull @DogSizeValid String size,
    @NotNull boolean neutering,
    @NotNull @DogGenderValid String gender,
    @NotNull @Max(100) int age,
    @NotNull boolean healthChecked,
    @NotNull @DateValid String registeredAt
) {}
