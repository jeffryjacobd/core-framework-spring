package com.jcb.web.handler;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

public interface LoginHandler {

	public Mono<ServerResponse> getSession(ServerRequest request);

	public Mono<ServerResponse> login(ServerRequest request);

	public Mono<ServerResponse> logout(ServerRequest request);

	Mono<Void> doLogoutHandler(WebFilterExchange webFilterExchange, AuthenticationException exception);
}