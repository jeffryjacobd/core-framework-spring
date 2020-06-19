package com.jcb.web.config;

import com.jcb.web.router.StaticFilesRouter;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ StaticFilesRouter.class })
public class RouterConfig {

}
