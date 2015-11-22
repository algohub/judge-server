package org.algohub.rest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;


@Configuration
public class RedisConfig {
  @Value("${redis.host}")
  private String host;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    JedisConnectionFactory factory = new JedisConnectionFactory();
    factory.setHostName(host);
    return factory;
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory,
      RedisSerializer redisSerializer) {
    StringRedisTemplate template = new StringRedisTemplate(factory);
    template.setValueSerializer(redisSerializer);
    template.afterPropertiesSet();
    return template;
  }

  @Bean
  public RedisSerializer redisSerializer() {
    return new GenericJackson2JsonRedisSerializer();
  }
}
