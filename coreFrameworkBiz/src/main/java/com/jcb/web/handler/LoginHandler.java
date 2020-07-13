package com.jcb.web.handler;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

public interface LoginHandler {

	public Mono<ServerResponse> getSession(ServerRequest request);

	public Mono<ServerResponse> login(ServerRequest request);
}