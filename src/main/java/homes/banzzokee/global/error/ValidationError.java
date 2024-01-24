package homes.banzzokee.global.error;

import java.util.Arrays;
import java.util.Objects;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;

public record ValidationError(String field, String reason) {

  public static ValidationError from(FieldError fieldError) {
    return new ValidationError(fieldError.getField(), fieldError.getDefaultMessage());
  }

  public static ValidationError from(ParameterValidationResult result) {
    MessageSourceResolvable error = result.getResolvableErrors().get(0);
    String field = Arrays.stream(Objects.requireNonNull(error.getArguments()))
        .findFirst().map(m -> (DefaultMessageSourceResolvable) m).get()
        .getDefaultMessage();
    String reason = error.getDefaultMessage();

    return new ValidationError(field, reason);
  }
}
