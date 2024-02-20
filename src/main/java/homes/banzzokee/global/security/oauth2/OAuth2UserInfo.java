package homes.banzzokee.global.security.oauth2;

import homes.banzzokee.domain.type.LoginType;
import homes.banzzokee.domain.type.Role;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.security.exception.SocialLoginAuthorizedException;
import jakarta.security.auth.message.AuthException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class OAuth2UserInfo {

  private final String email;
  private final String nickname;
  private final String profile;
  private final LoginType loginType;

  public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) throws AuthException {
    return switch (registrationId) {
      case "google" -> ofGoogle(attributes);
      default -> throw new SocialLoginAuthorizedException();
    };
  }

  private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
    return OAuth2UserInfo.builder()
        .email((String) attributes.get("email"))
        .profile((String) attributes.get("picture"))
        .nickname((String) attributes.get("name"))
        .loginType(LoginType.GOOGLE)
        .build();
  }

  public User toEntity() {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .profileImgUrl(profile)
        .role(Set.of(Role.ROLE_USER))
        .loginType(LoginType.GOOGLE)
        .build();
  }
}
