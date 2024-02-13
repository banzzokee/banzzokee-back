package homes.banzzokee.global.validator;

import homes.banzzokee.domain.type.BreedType;
import homes.banzzokee.global.validator.annotation.BreedValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class BreedValidator implements ConstraintValidator<BreedValid, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }

    if (value instanceof List<?> list) {
      for (Object element : list) {
        if (!(element instanceof String breed)) {
          return false;
        }
        if (!BreedType.contains(breed)) {
          return false;
        }
      }
      return true;
    }

    if (value instanceof String breed) {
      return BreedType.contains(breed);
    }


    return false;
  }
}
