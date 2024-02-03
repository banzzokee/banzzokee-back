package homes.banzzokee.global.validator.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import homes.banzzokee.global.validator.FileDuplicateValidator;
import jakarta.validation.Constraint;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {FileDuplicateValidator.class})
public @interface FileDuplicateValid {
  String message() default "중복 파일이 존재합니다.";

  Class[] groups() default {};

  Class[] payload() default {};
}
