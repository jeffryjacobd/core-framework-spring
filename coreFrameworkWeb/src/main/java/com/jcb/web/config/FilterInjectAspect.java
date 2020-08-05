package com.jcb.web.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.jcb.service.security.RSAEncryptionService;
import com.jcb.web.filter.EncryptionWebFilter;

@Aspect
@Component
public class FilterInjectAspect {

	@Autowired
	private RSAEncryptionService rsaService;

	@Autowired
	private ServerCodecConfigurer codecConfigurer;

	private EncryptionWebFilter encryptionFilter;

	private void setEncryptionWebFilter() {
		this.encryptionFilter = new EncryptionWebFilter(rsaService, codecConfigurer);
	}

	@Around("execution(public org.springframework.web.reactive.function.server.RouterFunction<org.springframework.web.reactive.function.server.ServerResponse> com.jcb.web.router.*.*(..))")
	@SuppressWarnings("unchecked")
	public Object injectEncryptionFilter(ProceedingJoinPoint pjp) throws Throwable {
		if (this.encryptionFilter == null) {
			setEncryptionWebFilter();
		}
		RouterFunction<ServerResponse> routerToBeInjected = (RouterFunction<ServerResponse>) pjp.proceed();
		return RouterFunctions.route().filter(encryptionFilter::filter).add(routerToBeInjected).build();
	}
}
