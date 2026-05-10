package com.pratyabhi.notification.router.dedup;

import java.time.Duration;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

/** Redis SET NX + EX — atomic claim with TTL. Maps to SDS §5.2. */
public class RedisDedupService implements DedupService {

  private final StringRedisTemplate redis;
  private final String keyPrefix;

  public RedisDedupService(StringRedisTemplate redis, String keyPrefix) {
    this.redis = redis;
    this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
  }

  @Override
  public boolean claim(String key, Duration ttl) {
    byte[] redisKey = (keyPrefix + key).getBytes();
    byte[] value = "1".getBytes();
    Expiration expiration = Expiration.from(ttl);
    Boolean acquired =
        redis.execute(
            (RedisCallback<Boolean>)
                conn -> conn.stringCommands().set(redisKey, value, expiration, SetOption.SET_IF_ABSENT));
    return Boolean.TRUE.equals(acquired);
  }
}
