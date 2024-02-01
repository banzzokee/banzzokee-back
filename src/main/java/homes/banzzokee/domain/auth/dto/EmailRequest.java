package homes.banzzokee.domain.auth.dto;

import homes.banzzokee.global.validator.annotation.EmailValid;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

  @EmailValid
  private String email;

}