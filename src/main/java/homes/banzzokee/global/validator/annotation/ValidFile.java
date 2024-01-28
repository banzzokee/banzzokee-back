package homes.banzzokee.global.validator.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import homes.banzzokee.global.validator.FileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = FileValidator.class)
public @interface ValidFile {

  /**
   * 허용할 MIME TYPE
   */
  String[] whiteList() default {};

  String message() default "파일 유효성 검사 실패";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
