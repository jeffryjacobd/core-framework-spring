package com.jcb.handlers.spring.web;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;

import reactor.core.publisher.Mono;

public class ReactiveApplicationWebHandler implements WebHandler {

	@Override
	public Mono<Void> handle(ServerWebExchange exchange) {
		// TODO Auto-generated method stub
		return null;
	}

}
