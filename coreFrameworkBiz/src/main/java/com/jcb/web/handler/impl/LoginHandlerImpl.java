package com.jcb.web.handler.impl;

import static com.jcb.entity.WebSession.ENCRYPTION_KEY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.session.WebSessionStore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcb.entity.SessionDataModel;
import com.jcb.entity.UserDetails;
import com.jcb.entity.UserModel;
import com.jcb.entity.WebSession;
import com.jcb.service.crypt.keygeneration.RSAKeyGeneratorService;
import com.jcb.service.security.UserAuthenticationService;
import com.jcb.web.handler.LoginHandler;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class LoginHandlerImpl implements LoginHandler {
	@Autowired
	private RSAKeyGeneratorService rsaKeyGenerator;

	@Autowired
	private WebSessionStore websessionStore;

	@Autowired
	private UserAuthenticationService userAuthenticationService;

	@Override
	public Mono<ServerResponse> getSession(ServerRequest request) {
		SessionDataModel model = new SessionDataModel();
		model.setKey("");
		// TO Do update from last routed path sessionDto
		model.setRoute("");
		return ServerResponse.ok().body(BodyInserters.fromValue(model));
	}

	@Override
	public Mono<ServerResponse> login(ServerRequest request) {
		return request.bodyToMono(UserModel.class).filter(userModel -> {
			return !((userModel.getUser().isBlank()) || userModel.getPassword().isBlank());
		}).flatMap(filteredUserModel -> {
			return this.userAuthenticationService.findByUsername(filteredUserModel.getUser()).filter(userDetail -> {
				return UserDetails.encoder.matches(filteredUserModel.getPassword(), userDetail.getPassword());
			});
		}).flatMap(userDetail -> {
			return saveLoginSession(request, userDetail);
		}).switchIfEmpty(Mono.defer(() -> ServerResponse.status(HttpStatus.UNAUTHORIZED).build()));

	}

	private Mono<ServerResponse> saveLoginSession(ServerRequest request,
			org.springframework.security.core.userdetails.UserDetails userDetail) {
		Mono<org.springframework.web.server.WebSession> webSession = request.exchange().getSession();
		return webSession.flatMap(session -> {
			session.getAttributes().put(WebSession.USER_NAME_KEY, userDetail.getUsername());
			session.getAttributes().put(WebSession.SAVE_ON_UPDATE, true);
			return this.websessionStore.updateLastAccessTime(session);
		}).flatMap(session -> ServerResponse.ok().build());
	}

	@Override
	public Mono<ServerResponse> logout(ServerRequest request) {
		Mono<org.springframework.web.server.WebSession> webSession = request.exchange().getSession();
		return webSession.flatMap(session -> {
			session.getAttributes().put(WebSession.USER_NAME_KEY, "");
			session.getAttributes().put(WebSession.SAVE_ON_UPDATE, true);
			return this.websessionStore.updateLastAccessTime(session);
		}).flatMap(session -> ServerResponse.ok().build());
	}

	@Override
	public Mono<Void> doLogoutHandler(WebFilterExchange webFilterExchange, AuthenticationException exception) {
		return Mono.defer(() -> {
			return rsaKeyGenerator.getRSAKey()
					.zipWith(webFilterExchange.getExchange().getSession().defaultIfEmpty(WebSession.builder().build()))
					.flatMap(rsaKeyAndSessionTuple -> {
						ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
						if (rsaKeyAndSessionTuple.getT2().getId().isBlank()) {
							response.getHeaders().clearContentHeaders();
							response.setStatusCode(HttpStatus.UNAUTHORIZED);
							return Mono.empty();
						}
						org.springframework.web.server.WebSession session = rsaKeyAndSessionTuple.getT2();
						session.getAttributes().put(ENCRYPTION_KEY, rsaKeyAndSessionTuple.getT1());
						SessionDataModel model = new SessionDataModel();
						byte[] responseBody = null;
						model.setRoute("login");
						model.setKey(rsaKeyAndSessionTuple.getT1().toPublicJWK().toJSONString());
						try {
							responseBody = new ObjectMapper().writeValueAsBytes(model);
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
						DataBuffer data = response.bufferFactory().wrap(responseBody);
						response.getHeaders().add("Content-Type", "application/json");
						response.setStatusCode(HttpStatus.UNAUTHORIZED);
						websessionStore.updateLastAccessTime(session).subscribeOn(Schedulers.parallel()).subscribe();
						return response.writeWith(Mono.just(data));
					});
		});
	}

}
