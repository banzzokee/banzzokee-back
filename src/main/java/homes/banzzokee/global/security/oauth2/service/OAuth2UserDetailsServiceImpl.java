package homes.banzzokee.global.security.oauth2.service;

import homes.banzzokee.domain.type.Role;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.security.UserDetailsImpl;
import homes.banzzokee.global.security.exception.SocialLoginAuthorizedException;
import homes.banzzokee.global.security.oauth2.OAuth2UserInfo;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static homes.banzzokee.global.error.ErrorCode.EMAIL_EXIST_DIFFERENT_LOGIN;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserDetailsServiceImpl extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();
    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    OAuth2UserInfo oAuth2UserInfo;
    try {
      oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2UserAttributes);
    } catch (AuthException e) {
      throw new SocialLoginAuthorizedException();
    }
    User user = getOrSave(oAuth2UserInfo);
    return new UserDetailsImpl(user, List.of(new SimpleGrantedAuthority(Role.ROLE_USER.name()))
        , oAuth2UserAttributes);
  }

  /**
   * 최초 소셜로그인 구분 로직
   */
  private User getOrSave(OAuth2UserInfo oAuth2UserInfo) {
    String email = oAuth2UserInfo.getEmail();
    Optional<User> findUser = userRepository.findByEmail(email);
    if (findUser.isPresent()) {
      User existingUser = findUser.get();
      if (!oAuth2UserInfo.getLoginType().equals(existingUser.getLoginType())) {
        OAuth2Error oauthError = new OAuth2Error(
            "invalid_request", EMAIL_EXIST_DIFFERENT_LOGIN.getMessage(), null);
        throw new OAuth2AuthenticationException(oauthError);
      }
      return existingUser;
    } else {
      User newUser = oAuth2UserInfo.toEntity();
      return userRepository.save(newUser);
    }
  }
}
