package homes.banzzokee.global.validator;

import homes.banzzokee.domain.type.DogSize;
import homes.banzzokee.global.validator.annotation.DogSizeValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DogSizeValidator implements ConstraintValidator<DogSizeValid, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return DogSize.contains(value);
  }
}
