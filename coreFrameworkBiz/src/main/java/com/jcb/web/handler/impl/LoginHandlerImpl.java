package com.jcb.web.handler.impl;

import static com.jcb.entity.WebSession.DEFAULT_SESSION_HEADER;

import java.util.List;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcb.entity.SessionDataModel;
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

	@Override
	public Mono<ServerResponse> logout(ServerRequest request) {
		return null;
	}

	@Override
	public Mono<Void> doLogoutHandler(WebFilterExchange webFilterExchange, AuthenticationException exception) {
		return Mono.defer(() -> {
			ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
			List<String> sessionList = webFilterExchange.getExchange().getRequest().getHeaders()
					.get(DEFAULT_SESSION_HEADER);
			if (sessionList == null || sessionList.get(0).isBlank()) {
				response.getHeaders().clearContentHeaders();
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				return Mono.empty();
			}
			SessionDataModel model = new SessionDataModel();
			byte[] responseBody = null;
			model.setRoute("login");
			try {
				responseBody = new ObjectMapper().writeValueAsBytes(model);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			DataBuffer data = response.bufferFactory().wrap(responseBody);
			response.getHeaders().add("Content-Type", "application/json");
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return response.writeWith(Mono.just(data));
		});
	}

}
