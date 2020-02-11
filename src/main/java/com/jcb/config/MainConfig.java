package com.jcb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ RedisCacheConfig.class, CassandraConfig.class, ThymeleafConfig.class })

public class MainConfig {

}
