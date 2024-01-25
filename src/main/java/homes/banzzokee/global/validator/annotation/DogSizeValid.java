package homes.banzzokee.global.validator.annotation;

import homes.banzzokee.global.validator.DogSizeValidator;
import jakarta.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DogSizeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DogSizeValid {
  String message() default "유효한 크기가 아닙니다.";
  Class[] groups() default {};
  Class[] payload() default {};
}
