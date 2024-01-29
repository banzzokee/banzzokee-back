package homes.banzzokee.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 이메일 인증 요청 dto
 */
@Getter
@Builder
@AllArgsConstructor
public class EmailVerifyDto {

  @NotBlank(message = "email 은 필수 입력 항목입니다.")
  @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "올바른 이메일 형식을 입력해주세요.")
  private final String email;

  @NotBlank(message = "인증 코드는 필수 입력 항목입니다.")
  @Pattern(regexp = "\\d{6}", message = "인증 코드는 6자리 숫자여야 합니다.")
  private final String code;

}