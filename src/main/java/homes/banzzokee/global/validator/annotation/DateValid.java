package homes.banzzokee.global.validator.annotation;

import homes.banzzokee.global.validator.DateValidator;
import jakarta.validation.Constraint;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateValid {
  String message() default "유효한 날짜 형식이 아닙니다.";
  String format() default "yyyy-MM-dd";
  Class[] groups() default {};
  Class[] payload() default {};
}
