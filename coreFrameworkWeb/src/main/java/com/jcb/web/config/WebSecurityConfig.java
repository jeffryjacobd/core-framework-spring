package com.jcb.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.WebFilter;

import com.jcb.web.handler.LoginHandler;

@EnableWebFluxSecurity
public class WebSecurityConfig {
	@Autowired
	private ServerAuthenticationConverter serverAuthenticationConverter;

	@Autowired
	private ReactiveAuthenticationManager authenticationManager;

	@Autowired
	private LoginHandler loginHandler;

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

		new NegatedServerWebExchangeMatcher(
				ServerWebExchangeMatchers.pathMatchers("/", "/*.js", "/*.js.map", "/*.ico"));
		// TO DO CSRF
		http.authorizeExchange().anyExchange().permitAll().and().csrf().disable();
		http.addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
		return http.build();
	}

	@Bean
	public WebFilter authenticationWebFilter() {
		AuthenticationWebFilter webFilter = new AuthenticationWebFilter(authenticationManager);
		webFilter.setServerAuthenticationConverter(serverAuthenticationConverter);
		webFilter.setAuthenticationFailureHandler(loginHandler::doLogoutHandler);
		return webFilter;
	}
}
