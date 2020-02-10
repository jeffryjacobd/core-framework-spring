package com.jcb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ CassandraConfig.class, ThymeleafConfig.class })

public class MainConfig {

}
