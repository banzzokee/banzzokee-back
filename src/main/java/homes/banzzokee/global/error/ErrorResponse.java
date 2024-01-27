package homes.banzzokee.global.error;

import static homes.banzzokee.global.error.ErrorCode.ARGUMENT_NOT_VALID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;

@Getter
@Builder
@RequiredArgsConstructor
public class ErrorResponse {

  private final ErrorCode error;
  private final String message;

  @JsonInclude(Include.NON_NULL)
  private final List<Object> errors;

  public static ErrorResponse of(ErrorCode errorCode) {
    return ErrorResponse.builder()
        .error(errorCode)
        .message(errorCode.getMessage())
        .build();
  }

  public static ErrorResponse of(ErrorCode errorCode, String message) {
    return ErrorResponse.builder()
        .error(errorCode)
        .message(message)
        .build();
  }

  public static <T> ErrorResponse of(List<T> errors) {
    if (errors.isEmpty()) {
      return null;
    }

    List<Object> errorList = null;

    if (errors.get(0) instanceof FieldError) {
      errorList = Collections.singletonList(
          errors.stream()
              .map(err -> ValidationError.from((FieldError) err))
      );
    } else if (errors.get(0) instanceof ParameterValidationResult) {
      errorList = Collections.singletonList(
          errors.stream()
              .map(m -> ValidationError.from((ParameterValidationResult) m))
      );
    }

    return ErrorResponse.builder()
        .error(ARGUMENT_NOT_VALID)
        .message(ARGUMENT_NOT_VALID.getMessage())
        .errors(errorList)
        .build();
  }
}
