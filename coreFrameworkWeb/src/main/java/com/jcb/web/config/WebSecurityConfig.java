package com.jcb.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.WebFilter;

import com.jcb.service.security.AESEncryptionService;
import com.jcb.service.security.RSAEncryptionService;
import com.jcb.web.filter.EncryptionWebFilter;
import com.jcb.web.handler.LoginHandler;

@EnableWebFlux
@EnableWebFluxSecurity
public class WebSecurityConfig {
	@Autowired
	private ServerAuthenticationConverter serverAuthenticationConverter;

	@Autowired
	private ReactiveAuthenticationManager authenticationManager;

	@Autowired
	private LoginHandler loginHandler;

	@Autowired
	private RSAEncryptionService rsaService;

	@Autowired
	private AESEncryptionService aesService;

	@Autowired
	private ServerCodecConfigurer codecConfigurer;

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public WebFilter encryptionWebFilter() {
		return new EncryptionWebFilter(rsaService, aesService, codecConfigurer);
	}

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		// TO DO CSRF
		http.authorizeExchange().anyExchange().permitAll().and().csrf().disable();
		http.addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
		return http.build();
	}

	private WebFilter authenticationWebFilter() {
		AuthenticationWebFilter webFilter = new AuthenticationWebFilter(authenticationManager);
		webFilter.setServerAuthenticationConverter(serverAuthenticationConverter);
		webFilter.setAuthenticationFailureHandler(loginHandler::doLogoutHandler);
		webFilter.setRequiresAuthenticationMatcher(new NegatedServerWebExchangeMatcher(
				ServerWebExchangeMatchers.pathMatchers("/", "/*.css", "/*.js", "/*.js.map", "/*.ico", "/login")));
		return webFilter;
	}
}
