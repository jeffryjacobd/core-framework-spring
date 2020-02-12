package com.jcb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@Import({ RedisCacheConfig.class, CassandraConfig.class, ThymeleafConfig.class })

public class MainConfig {

}
