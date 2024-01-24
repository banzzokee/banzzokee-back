package homes.banzzokee.domain.adoption.dto;

import homes.banzzokee.global.validator.annotation.BreedValid;
import homes.banzzokee.global.validator.annotation.DateValid;
import homes.banzzokee.global.validator.annotation.DogGenderValid;
import homes.banzzokee.global.validator.annotation.DogSizeValid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public record RegisterAdoptionDto (
    @NotNull Long shelterId,
    @NotNull @Size(max = 50) String title,
    @NotNull @Size(max = 500) String content,
    @BreedValid String breed,
    @DogSizeValid String size,
    boolean neutering,
    @DogGenderValid String gender,
    @Max(100) int age,
    boolean healthChecked,
    @DateValid String registeredAt

) {

}
