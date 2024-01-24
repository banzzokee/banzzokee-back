package homes.banzzokee.global.validator.annotation;

import homes.banzzokee.global.validator.BreedValidator;
import homes.banzzokee.global.validator.DateValidator;
import jakarta.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = BreedValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface BreedValid {
  String message() default "유효한 견종이 아닙니다.";
  Class[] groups() default {};
  Class[] payload() default {};
}
