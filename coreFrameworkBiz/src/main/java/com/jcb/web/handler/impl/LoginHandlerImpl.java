package com.jcb.web.handler.impl;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.jcb.web.handler.LoginHandler;

import reactor.core.publisher.Mono;

public class LoginHandlerImpl implements LoginHandler {

	@Override
	public Mono<ServerResponse> getSession(ServerRequest request) {
		return null;
	}

	@Override
	public Mono<ServerResponse> login(ServerRequest request) {
		return null;
	}

}
