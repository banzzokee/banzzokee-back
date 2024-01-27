package homes.banzzokee.global.validator.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@ValidFile(whiteList = {"image/jpeg", "image/tiff",
    "image/png", "image/gif", "image/bmp", "image/webp"}, message = "이미지 형식이 아닙니다.")
@Target({PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {})
public @interface ImageFile {

  String message() default "";

  Class[] groups() default {};

  Class[] payload() default {};
}
