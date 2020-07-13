package com.jcb.web.handler.config;

import org.springframework.context.annotation.Bean;

import com.jcb.web.handler.LoginHandler;
import com.jcb.web.handler.impl.LoginHandlerImpl;

public class HandlerConfig {
	@Bean
	public LoginHandler loginHandler() {
		return new LoginHandlerImpl();
	}
}
