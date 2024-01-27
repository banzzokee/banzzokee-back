package homes.banzzokee.global.validator;

import static homes.banzzokee.global.validator.constant.RegexPattern.PASSWORD;

import homes.banzzokee.global.validator.annotation.Password;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

  @Override
  public boolean isValid(String value,
      ConstraintValidatorContext context) {
    if (value == null) {
      return false;
    }

    return value.matches(PASSWORD);
  }
}
