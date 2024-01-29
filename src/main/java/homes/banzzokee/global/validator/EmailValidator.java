package homes.banzzokee.global.validator;

import homes.banzzokee.global.validator.annotation.EmailValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static homes.banzzokee.global.validator.constant.RegexPattern.EMAIL;

public class EmailValidator implements ConstraintValidator<EmailValid, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return false;
    }
    return value.matches(EMAIL.getPattern());
  }
}
