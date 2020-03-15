package com.jcb.config;

import com.jcb.handlers.spring.bean.dao.DaoTemplateBeanCreator;
import com.jcb.handlers.spring.bean.redis.RedisTemplateBeanCreator;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@Import({ RedisCacheConfig.class, RedisTemplateBeanCreator.class, CassandraConfig.class, DaoTemplateBeanCreator.class,
	ThymeleafConfig.class })

public class MainConfig {

}
