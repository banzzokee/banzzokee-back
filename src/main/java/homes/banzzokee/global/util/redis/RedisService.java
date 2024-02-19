package homes.banzzokee.global.util.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

  private final RedisTemplate<String, String> redisTemplate;

  public void setData(String key, String value, long duration) {
    redisTemplate.opsForValue().set(key, value, duration, TimeUnit.SECONDS);
  }

  public void setDataExpire(String key, String value, long duration) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    valueOperations.set(key, value, duration, TimeUnit.SECONDS);
  }

  public String getData(String key) {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    return valueOperations.get(key);
  }

  public void deleteKey(String key) {
    redisTemplate.delete(key);
  }

  public void setRefreshToken(String key, String token, long duration) {
    redisTemplate.opsForValue().set(key, token, duration, TimeUnit.SECONDS);
  }

  public String getRefreshToken(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  public void deleteRefreshToken(String key) {
    redisTemplate.delete(key);
  }

  public void addToBlacklist(String token) {
    redisTemplate.opsForValue().set(token, "blacklisted");
  }

  public boolean isBlacklisted(String token) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(token));
  }

  public boolean isRefreshTokenExist(String email, String token) {
    return this.getRefreshToken(email) != null && this.getRefreshToken(email).equals(token);
  }

  public boolean hasUserEmail(String email) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(email));
  }
}