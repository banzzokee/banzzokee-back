package homes.banzzokee.global.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse<T> {

  private ErrorCode error;
  private String message;

  public static <T> ErrorResponse<T> fail(ErrorCode errorCode) {
    return ErrorResponse.<T>builder()
        .error(errorCode)
        .message(errorCode.getMessage())
        .build();
  }
}
