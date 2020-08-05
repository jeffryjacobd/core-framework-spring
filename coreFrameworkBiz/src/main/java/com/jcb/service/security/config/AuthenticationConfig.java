package com.jcb.service.security.config;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.NoSuchPaddingException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;

import com.jcb.service.security.AESEncryptionService;
import com.jcb.service.security.RSAEncryptionService;
import com.jcb.service.security.UserAuthenticationService;
import com.jcb.service.security.impl.AESEncryptionServiceImpl;
import com.jcb.service.security.impl.RSAEncryptionServiceImpl;
import com.jcb.service.security.impl.SessionBasedAuthenticationManager;
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
					return new PreAuthenticatedAuthenticationToken(webSession, null);
				});
			}
		};
	}

	@Bean
	public ReactiveAuthenticationManager authenticationManager() {
		return new SessionBasedAuthenticationManager();
	}

	@Bean
	public AESEncryptionService aesEncryptionService()
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
		return new AESEncryptionServiceImpl();
	}

	@Bean
	public RSAEncryptionService rsaEncryptionService()
			throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
		return new RSAEncryptionServiceImpl();
	}
}
