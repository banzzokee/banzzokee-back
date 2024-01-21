package homes.banzzokee.global.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

  private final ErrorCode error;
  private final String message;

  public static ErrorResponse of(ErrorCode errorCode) {
    return ErrorResponse.builder()
        .error(errorCode)
        .message(errorCode.getMessage())
        .build();
  }
}
