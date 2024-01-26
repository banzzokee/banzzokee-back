package homes.banzzokee.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EmailDto {
  /**
   * email 인증할 이메일
   */

  @NotBlank(message = "email 은 필수 입력 항목입니다.")
  @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "올바른 이메일 형식을 입력해주세요.")
  private final String email;

}