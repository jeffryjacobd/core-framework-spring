package com.jcb.config;

import com.jcb.handlers.spring.bean.dao.DaoTemplateBeanCreator;
import com.jcb.handlers.spring.bean.dto.RedisTemplateBeanCreator;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@EnableAspectJAutoProxy
@Import({ RedisCacheConfig.class, RedisTemplateBeanCreator.class, CassandraConfig.class, DaoTemplateBeanCreator.class,
	CassandraUtilityConfig.class, EmbeddedTomcatConfiguration.class })
public class MainConfig {

}
