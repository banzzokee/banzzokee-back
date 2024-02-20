package homes.banzzokee.global.security.oauth2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NicknameRequest {

  @Length(max = 10, message = "닉네임은 최대 10자리까지 가능합니다.")
  private String nickname;

}
