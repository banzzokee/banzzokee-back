package homes.banzzokee.global.security.jwt;

import homes.banzzokee.global.security.UserDetailsServiceImpl;
import homes.banzzokee.global.security.exception.AccessTokenExpiredException;
import homes.banzzokee.global.security.exception.RefreshTokenExpiredException;
import homes.banzzokee.global.security.exception.TokenInvalidException;
import homes.banzzokee.global.util.redis.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  public static final int BEARER_LENGTH = 7;

  private final RedisService redisService;
  private final UserDetailsServiceImpl userDetailsService;

  @Value("${jwt.secretKey}")
  private String secretKey;

  @Value("${jwt.accessTokenExpire}")
  private long accessTokenExpire;

  @Value("${jwt.refreshTokenExpire}")
  private long refreshTokenExpire;

  public String createAccessToken(String email) {
    return createToken(email, accessTokenExpire);
  }

  public String createRefreshToken(String email) {
    return createToken(email, refreshTokenExpire);
  }

  private String createToken(String email, long validity) {
    Claims claims = Jwts.claims().setSubject(email);
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + validity);
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(email)
        .setExpiration(expiryDate)
        .signWith(getSecretKey())
        .compact();
  }

  public void validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(getSecretKey()).build().parseClaimsJws(token);
    } catch (ExpiredJwtException e) {
      if (!isRefreshToken(token)) {
        throw new RefreshTokenExpiredException();
      } else {
        throw new AccessTokenExpiredException();
      }
    } catch (JwtException e) {
      throw new TokenInvalidException();
    }
  }

  public String getUserEmailFromToken(String token) {
    Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(getSecretKey())
        .build().parseClaimsJws(token);
    return claimsJws.getBody().getSubject();
  }

  public SecretKey getSecretKey() {
    return Keys.hmacShaKeyFor(this.secretKey.getBytes(StandardCharsets.UTF_8));
  }

  private boolean isRefreshToken(String token) {
    return redisService.isRefreshTokenExist(getUserEmailFromToken(token), token);
  }

  /**
   * 토큰 인증 정보 조회
   */
  public Authentication getAuthentication(String token) {
    UserDetails userDetails =
        userDetailsService.loadUserByUsername(
            // claim 의 subject 에 저장되어있는 email
            getUserEmailFromToken(token)
        );

    return new UsernamePasswordAuthenticationToken(
        userDetails, "", userDetails.getAuthorities()
    );
  }

  public String removeBearerFromToken(String token) {
    return token.substring(BEARER_LENGTH);
  }
}