package homes.banzzokee.global.security.oauth2.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2Response {

  private final String accessToken;
  private final String refreshToken;
  private final boolean firstLogin;

}
