package homes.banzzokee.global.validator;

import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.global.validator.annotation.BreedValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BreedValidator implements ConstraintValidator<BreedValid, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return BreedType.contains(value);
  }
}
