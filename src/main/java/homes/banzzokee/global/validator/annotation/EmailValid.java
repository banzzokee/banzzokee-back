package homes.banzzokee.global.validator.annotation;

import homes.banzzokee.global.validator.EmailValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
public @interface EmailValid {

  String message() default "올바른 이메일 형식을 입력해주세요.";

  Class[] groups() default {};

  Class[] payload() default {};
}
