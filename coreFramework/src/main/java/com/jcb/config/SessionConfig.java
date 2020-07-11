package com.jcb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import com.jcb.handlers.spring.session.initializer.WebSessionManager;
import com.jcb.handlers.spring.session.initializer.WebSessionStore;

@Import({ WebSessionStore.class, WebSessionManager.class })
@Component
public class SessionConfig {
	public static final String DEFAULT_SESSION_HEADER = "X-Auth-Token";

	@Bean
	public WebSessionIdResolver sessionHeaderIdResolver() {
		HeaderWebSessionIdResolver sessionIdResolver = new HeaderWebSessionIdResolver();
		sessionIdResolver.setHeaderName(DEFAULT_SESSION_HEADER);
		return sessionIdResolver;
	}
}
