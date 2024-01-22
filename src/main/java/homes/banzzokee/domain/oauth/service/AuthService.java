package homes.banzzokee.domain.oauth.service;

import homes.banzzokee.domain.oauth.dto.TokenDto;
import homes.banzzokee.domain.oauth.exception.TokenRequiredException;
import homes.banzzokee.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

@Service
@RequiredArgsConstructor
public class AuthService {

  @Value("${oauth2.client.registration.google.client_id}")
  private String clientId;

  @Value("${oauth2.client.registration.google.client_secret}")
  private String clientSecret;

  @Value("${oauth2.client.registration.google.redirect_uri}")
  private String redirectUri;

  public TokenDto getGoogleToken(String code) {
    String googleTokenUrl = "https://oauth2.googleapis.com/token";
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("code", code);
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);
    params.add("redirect_uri", redirectUri);
    params.add("grant_type", "authorization_code");
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
    ResponseEntity<TokenDto> tokenResponse = restTemplate.postForEntity(googleTokenUrl, request, TokenDto.class);
    if (tokenResponse.getStatusCode().value() != 200) {
      throw new TokenRequiredException(ErrorCode.TOKEN_REQUIRED);
    }
    return tokenResponse.getBody();
  }
}