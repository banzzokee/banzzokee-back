package homes.banzzokee.global.validator;

import homes.banzzokee.domain.type.DogGender;
import homes.banzzokee.global.validator.annotation.DogGenderValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DogGenderValidator implements ConstraintValidator<DogGenderValid, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return DogGender.contains(value);
  }
}
