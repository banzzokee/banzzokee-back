package homes.banzzokee.domain.auth.dto;

import homes.banzzokee.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignupDto {

  @NotBlank(message = "email 은 필수 입력 항목입니다.")
  @Email(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "올바른 이메일 형식을 입력해주세요.")
  private final String email;

  @NotBlank(message = "password 는 필수 입력 항목입니다.")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()-_=+]).{8,20}$",
      message = "password 는 소문자, 대문자, 숫자, 특수문자 각각 최소 1개 이상을 포함하는 8자리 이상 20자리 이하로 설정해야 합니다.")
  private final String password;

  @NotBlank(message = "password 확인은 필수 입력 항목입니다.")
  private final String confirmPassword;

  @Size(max = 10, message = "nickname 은 10자리 이내로 작성해주세요.")
  @NotBlank(message = "nickname 은 필수 입력 항목입니다.")
  private final String nickname;

  public User toEntity() {
    return User.builder()
        .email(this.email)
        .password(this.password)
        .nickname(this.nickname)
        .build();
  }
}