package com.jcb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import com.jcb.biz.config.BizMainConfig;
import com.jcb.handlers.spring.bean.dao.DaoTemplateBeanCreator;
import com.jcb.handlers.spring.session.config.SessionConfig;
import com.jcb.web.config.WebMainConfig;

@Configuration
@EnableAspectJAutoProxy
@Import({ CassandraConfig.class, DaoTemplateBeanCreator.class, CassandraUtilityConfig.class,
		EmbeddedTomcatConfiguration.class, SessionConfig.class, WebMainConfig.class, BizMainConfig.class })
public class MainConfig {

}
