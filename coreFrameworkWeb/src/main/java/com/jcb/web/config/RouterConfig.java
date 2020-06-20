package com.jcb.web.config;

import com.jcb.web.router.StaticFilesRouter;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ StaticFilesRouter.class, WebSecurityConfig.class })
public class RouterConfig {

}
