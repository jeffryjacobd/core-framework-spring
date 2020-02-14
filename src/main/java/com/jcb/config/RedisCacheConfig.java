package com.jcb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCacheConfig {

    @Bean("reactiveRedisConnectionFactory")
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
	return new LettuceConnectionFactory();
    }

    @Bean("redisSerializationContextBuilder")
    public RedisSerializationContext.RedisSerializationContextBuilder redisSerializationContextBuilder() {
	return RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
    }

}
