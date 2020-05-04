package com.jcb.config;

import static com.jcb.constants.SystemPropertyConstants.REDIS_POINT_PROPERTY;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCacheConfig {

    @Bean("reactiveRedisConnectionFactory")
    ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
	String redisPoint = System.getProperty(REDIS_POINT_PROPERTY);
	if (!StringUtils.isEmpty(redisPoint)) {
	    return new LettuceConnectionFactory(redisPoint.substring(0, redisPoint.indexOf(":")).trim(),
		    Integer.valueOf(redisPoint.substring(redisPoint.indexOf(":") + 1, redisPoint.length()).trim()));
	}
	return new LettuceConnectionFactory();
    }

    @SuppressWarnings("rawtypes")
    @Bean("redisSerializationContextBuilder")
    RedisSerializationContext.RedisSerializationContextBuilder redisSerializationContextBuilder() {
	return RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
    }

}
