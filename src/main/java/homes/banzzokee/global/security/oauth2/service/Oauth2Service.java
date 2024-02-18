package homes.banzzokee.global.security.oauth2.service;

import homes.banzzokee.domain.auth.dto.TokenResponse;
import homes.banzzokee.domain.auth.exception.EmailNotFoundException;
import homes.banzzokee.domain.user.dao.UserRepository;
import homes.banzzokee.domain.user.entity.User;
import homes.banzzokee.global.security.jwt.JwtTokenProvider;
import homes.banzzokee.global.security.oauth2.dto.NicknameRequest;
import homes.banzzokee.global.util.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class Oauth2Service {

  public static final int BEARER_LENGTH = 7;

  private final RedisService redisService;
  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional
  public TokenResponse signup(String token, NicknameRequest nicknameRequest) {
    String email = jwtTokenProvider.getUserEmailFromToken(token.substring(BEARER_LENGTH));
    User user = userRepository.findByEmail(email)
        .orElseThrow(EmailNotFoundException::new);
    user.updateNickname(nicknameRequest.getNickname());
    userRepository.save(user);
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
    redisService.setRefreshToken(
        user.getEmail(), refreshToken, jwtTokenProvider.getRefreshTokenExpire());
    return TokenResponse.builder()
        .accessToken(jwtTokenProvider.createAccessToken(user.getEmail()))
        .refreshToken(refreshToken)
        .build();
  }
}
