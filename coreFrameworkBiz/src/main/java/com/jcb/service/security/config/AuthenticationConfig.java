package com.jcb.service.security.config;

import static com.jcb.entity.WebSession.USER_NAME_KEY;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;

import com.jcb.service.security.UserAuthenticationService;
import com.jcb.service.security.impl.UserAuthenticationServiceImpl;

import reactor.core.publisher.Mono;

@Configuration
public class AuthenticationConfig {
	@Bean
	public UserAuthenticationService authenticationService() {
		return new UserAuthenticationServiceImpl();
	}

	@Bean
	public ServerAuthenticationConverter sessionAuthenticationMapper() {
		return new ServerAuthenticationConverter() {
			@Override
			public Mono<Authentication> convert(ServerWebExchange exchange) {
				return exchange.getSession().map(webSession -> {
					return new AnonymousAuthenticationToken(webSession.getAttributes().get(USER_NAME_KEY).toString(),
							webSession, List.of());
				});
			}
		};
	}
}
