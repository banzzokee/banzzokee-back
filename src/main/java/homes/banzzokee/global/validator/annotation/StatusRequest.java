package homes.banzzokee.global.validator.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import homes.banzzokee.global.validator.StatusRequestValidator;
import jakarta.validation.Constraint;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = StatusRequestValidator.class)
public @interface StatusRequest {

  String message() default "유효한 상태변경 요청이 아닙니다.";

  Class[] groups() default {};

  Class[] payload() default {};
}
