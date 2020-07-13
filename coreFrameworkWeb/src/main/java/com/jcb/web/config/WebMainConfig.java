package com.jcb.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.jcb.web.router.LoginRouter;
import com.jcb.web.router.StaticFilesRouter;

@Configuration
@Import({ StaticFilesRouter.class, LoginRouter.class, WebSecurityConfig.class })
public class WebMainConfig {

}
