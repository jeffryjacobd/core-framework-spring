package com.jcb.service.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jcb.service.security.UserAuthenticationService;
import com.jcb.service.security.impl.UserAuthenticationServiceImpl;

@Configuration
public class AutheticationConfig {
	@Bean
	public UserAuthenticationService authenticationService() {
		return new UserAuthenticationServiceImpl();
	}
}
