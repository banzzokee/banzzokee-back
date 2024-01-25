package homes.banzzokee.global.validator;

import homes.banzzokee.global.validator.annotation.DateValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DateValidator implements ConstraintValidator<DateValid, String> {
  private String dateFormat;


  @Override
  public void initialize(DateValid constraintAnnotation) {
    dateFormat = constraintAnnotation.format();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    try {
      DateFormat sdf = new SimpleDateFormat(this.dateFormat);
      sdf.parse(value);
    } catch (Exception e) {
      return false;
    }
    return true;
  }
}
