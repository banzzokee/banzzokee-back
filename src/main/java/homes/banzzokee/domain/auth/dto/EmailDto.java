package homes.banzzokee.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailDto {
  /**
   * email 인증할 이메일
   */
public record EmailDto(

    @NotBlank(message = "email 은 필수 입력 항목입니다.")
    @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "올바른 이메일 형식을 입력해주세요.")
    String email) {

}