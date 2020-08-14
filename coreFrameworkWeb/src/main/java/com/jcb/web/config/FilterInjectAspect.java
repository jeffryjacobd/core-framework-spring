package com.jcb.web.config;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.server.EntityResponse;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.jcb.service.security.AESEncryptionService;
import com.jcb.service.security.RSAEncryptionService;
import com.jcb.web.filter.EncryptionWebFilter;

@Aspect
@Component
public class FilterInjectAspect {

	@Autowired
	private RSAEncryptionService rsaService;

	@Autowired
	private AESEncryptionService aesService;

	@Autowired
	private ServerCodecConfigurer codecConfigurer;

	private EncryptionWebFilter encryptionFilter;

	private void setEncryptionWebFilter() {
		this.encryptionFilter = new EncryptionWebFilter(rsaService, aesService, codecConfigurer);
	}

	@Around("execution(public org.springframework.web.reactive.function.server.RouterFunction<org.springframework.web.reactive.function.server.ServerResponse> com.jcb.web.router.*.*(..))")
	@SuppressWarnings("unchecked")
	public Object injectEncryptionFilter(ProceedingJoinPoint pjp) throws Throwable {
		if (this.encryptionFilter == null) {
			setEncryptionWebFilter();
		}
		RouterFunction<ServerResponse> routerToBeInjected = (RouterFunction<ServerResponse>) pjp.proceed();
		return RouterFunctions.route().filter(encryptionFilter::filter).add(routerToBeInjected)
				.after((request, response) -> {
					return addRequiredContentType(request, response);
				}).build();
	}

	@SuppressWarnings("unchecked")
	private ServerResponse addRequiredContentType(ServerRequest request, ServerResponse response) {
		if (request.path().endsWith(".css")) {
			BodyInserter<?, ? super ServerHttpResponse> inserter = ((EntityResponse<ClassPathResource>) response)
					.inserter();
			response = ServerResponse.from(response).headers(responseHeaders -> {
				responseHeaders.put("content-type", Arrays.asList("text/css"));
			}).body(inserter).block();
		}
		return response;
	}
}
