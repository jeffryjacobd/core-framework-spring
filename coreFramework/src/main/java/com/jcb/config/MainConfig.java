package com.jcb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.config.EnableWebFlux;

import com.jcb.biz.config.BizMainConfig;
import com.jcb.handlers.jwt.JwtAuthenticationManager;
import com.jcb.handlers.spring.bean.dao.DaoTemplateBeanCreator;
import com.jcb.web.config.WebMainConfig;

@Configuration
@EnableWebFlux
@EnableAspectJAutoProxy
@Import({ CassandraConfig.class, DaoTemplateBeanCreator.class, CassandraUtilityConfig.class,
		EmbeddedTomcatConfiguration.class, SessionConfig.class, JwtAuthenticationManager.class, WebMainConfig.class,
		BizMainConfig.class })
public class MainConfig {

}
