package com.jcb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;

import com.jcb.handlers.spring.bean.redis.RedisTemplateBeanCreator;

@Configuration
@EnableWebFlux
@Import({ RedisCacheConfig.class, RedisTemplateBeanCreator.class, CassandraConfig.class, ThymeleafConfig.class })

public class MainConfig {

}
