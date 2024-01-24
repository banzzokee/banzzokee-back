package homes.banzzokee.global.util.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

  private final RedisTemplate<String, Object> redisTemplate;

  public RedisService(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public void setData(String key, Object value, long duration) {
    redisTemplate.opsForValue().set(key, value, duration, TimeUnit.SECONDS);
  }

  public void setDataExpire(String key, Object value, long duration) {
    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
    valueOperations.set(key, value, duration, TimeUnit.SECONDS);
  }

  public Object getData(String key) {
    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
    return valueOperations.get(key);
  }

  public void deleteKey(String key) {
    redisTemplate.delete(key);
  }
}