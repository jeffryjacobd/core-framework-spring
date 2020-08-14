package com.jcb.web.filter;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.handler.DefaultWebFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcb.entity.SessionDataModel;
import com.jcb.entity.WebSession;
import com.jcb.service.security.AESEncryptionService;
import com.jcb.service.security.RSAEncryptionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class EncryptionWebFilter implements WebFilter, HandlerFilterFunction<ServerResponse, ServerResponse> {

	private final RSAEncryptionService rsaService;

	private final ServerCodecConfigurer codecConfigurer;

	private final AESEncryptionService aesService;

	public EncryptionWebFilter(RSAEncryptionService rsaService, AESEncryptionService aesService,
			ServerCodecConfigurer codecConfigurer) {
		this.codecConfigurer = codecConfigurer;
		this.rsaService = rsaService;
		this.aesService = aesService;
	}

	@Override
	public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
		ServerWebExchange exchange = request.exchange();
		return Mono.defer(() -> ServerWebExchangeMatchers.pathMatchers("/", "/*.css", "/*.js", "/*.js.map", "/*.ico")
				.matches(exchange).flatMap(matcher -> {
					if (!matcher.isMatch()) {
						if (request.path().endsWith("/login")) {
							return decryptLoginFilteredRequest(request).flatMap(changedRequest -> {
								return Mono.defer(() -> next.handle(changedRequest));
							}).switchIfEmpty(Mono.defer(() -> next.handle(request)));
						} else if (request.path().endsWith("/getSession")) {
							return decryptHandShakeFilteredRequest(request).flatMap(changedRequest -> {
								return Mono.defer(() -> next.handle(changedRequest));
							}).switchIfEmpty(Mono.defer(() -> next.handle(request)));
						} else {
							return Mono.defer(() -> next.handle(request));
						}
					}
					return Mono.defer(() -> next.handle(request));
				}));
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		DispatcherHandler handler = (DispatcherHandler) ((DefaultWebFilterChain) chain).getHandler();
		if (handler.getHandlerMappings() != null) {
			return Mono.defer(() -> Flux.fromIterable(handler.getHandlerMappings())
					.concatMap(mapping -> mapping.getHandler(exchange)).filter(handlerObject -> {
						return !(handlerObject.getClass().getName()
								.startsWith("org.springframework.web.reactive.function.server.HandlerFilterFunction"));
					}).next().defaultIfEmpty(new String("skip")).flatMap((testObject) -> {
						if ((testObject instanceof String) && (new String((String) testObject).equals("skip"))) {
							return Mono.defer(() -> chain.filter(exchange));
						}
						return Mono.defer(() -> ServerWebExchangeMatchers
								.pathMatchers("/", "/*.js", "/*.js.map", "/*.ico", "/getSession").matches(exchange)
								.flatMap(matcher -> {
									if (!matcher.isMatch()) {
										return ServerWebExchangeMatchers.pathMatchers("/login").matches(exchange)
												.filter(matchedPath -> matchedPath.isMatch())
												.flatMap(filteredRequest -> {
													return Mono.defer(() -> decryptLoginFilteredRequest(exchange));
												}).flatMap(changedExchange -> {
													return Mono.defer(() -> chain.filter(exchange.mutate()
															.request(changedExchange.getRequest()).build()));
												}).switchIfEmpty(Mono.defer(() -> chain.filter(exchange)));
									}
									return Mono.defer(() -> chain.filter(exchange));
								}));
					}));
		}
		return chain.filter(exchange);
	}

	private Mono<ServerWebExchange> decryptLoginFilteredRequest(ServerWebExchange exchange) {
		return Mono.just(exchange).flatMap((exchangeToMutate) -> {
			return exchangeToMutate.getSession();
		}).map(session -> {
			return (KeyPair) session.getAttributes().get(WebSession.ENCRYPTION_KEY);
		}).zipWhen(rsaKey -> {
			ServerRequest filteredRequest = ServerRequest.create(exchange, codecConfigurer.getReaders());
			return filteredRequest.bodyToMono(String.class);
		}, (rsaKeyCombinator, encryptedString) -> {
			return Tuples.of(rsaKeyCombinator, Hex.decode(encryptedString));
		}).flatMap(byteArrayRSATuple -> {
			return this.rsaService.decrypt(byteArrayRSATuple.getT2(), byteArrayRSATuple.getT1().getPrivate());
		}).map(decryptedBytes -> {
			return ServerRequest.from(ServerRequest.create(exchange, codecConfigurer.getReaders())).headers(header -> {
				header.setContentType(MediaType.APPLICATION_JSON);
			}).body(new String(decryptedBytes, StandardCharsets.UTF_8)).build().exchange();
		});
	}

	private Mono<ServerRequest> decryptLoginFilteredRequest(ServerRequest request) {
		return Mono.just(request).flatMap((requestToMutate) -> {
			return requestToMutate.session();
		}).map(session -> {
			return (KeyPair) session.getAttributes().get(WebSession.ENCRYPTION_KEY);
		}).zipWhen(rsaKey -> {
			return request.bodyToMono(String.class);
		}, (rsaKeyCombinator, encryptedString) -> {
			return Tuples.of(rsaKeyCombinator, Hex.decode(encryptedString));
		}).flatMap(byteArrayRSATuple -> {
			return this.rsaService.decrypt(byteArrayRSATuple.getT2(), byteArrayRSATuple.getT1().getPrivate());
		}).map(decryptedBytes -> {
			return ServerRequest.from(request).headers(header -> {
				header.setContentType(MediaType.APPLICATION_JSON);
			}).body(new String(decryptedBytes, StandardCharsets.UTF_8)).build();
		});
	}

	private Mono<ServerRequest> decryptHandShakeFilteredRequest(ServerRequest request) {
		return Flux.fromIterable(request.headers().header(WebSession.DEFAULT_SESSION_HEADER)).next()
				.switchIfEmpty(Mono.defer(() -> request.session().map(session -> {
					return session.getId();
				}))).zipWhen(sessionId -> {
					return request.bodyToMono(String.class);
				}, (sessionIdCombinator, encryptedString) -> {
					return Tuples.of(sessionIdCombinator, encryptedString.getBytes(StandardCharsets.UTF_8));
				}).flatMap(sessionIdEncryptedBytesTuple -> {
					return this.aesService.decrypt(sessionIdEncryptedBytesTuple.getT2(),
							sessionIdEncryptedBytesTuple.getT1().getBytes(StandardCharsets.UTF_8), true);
				}).map(decrypedBytes -> {
					SessionDataModel model = new SessionDataModel();
					model.setKey(new String(decrypedBytes, StandardCharsets.UTF_8));
					return model;
				}).map(decryptedModel -> {
					String jsonString = "";
					try {
						jsonString = new ObjectMapper().writeValueAsString(decryptedModel);
					} catch (Exception e) {
						e.printStackTrace();
						Mono.error(e);
					}
					return ServerRequest.from(request).headers(header -> {
						header.setContentType(MediaType.APPLICATION_JSON);
					}).body(jsonString).build();
				});
	}

}
