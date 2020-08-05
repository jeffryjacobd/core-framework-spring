package com.jcb.web.filter;

import static com.jcb.entity.WebSession.ENCRYPTION_KEY;

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

import com.jcb.service.security.RSAEncryptionService;
import com.jcb.web.router.LoginRouter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class EncryptionWebFilter implements WebFilter, HandlerFilterFunction<ServerResponse, ServerResponse> {

	private final RSAEncryptionService rsaService;

	private final ServerCodecConfigurer codecConfigurer;

	public EncryptionWebFilter(RSAEncryptionService rsaService, ServerCodecConfigurer codecConfigurer) {
		this.codecConfigurer = codecConfigurer;
		this.rsaService = rsaService;
	}

	@Override
	public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
		ServerWebExchange exchange = request.exchange();
		return Mono.defer(() -> ServerWebExchangeMatchers
				.pathMatchers("/", "/*.js", "/*.js.map", "/*.ico", "/getSession").matches(exchange).flatMap(matcher -> {
					if (!matcher.isMatch()) {
						return ServerWebExchangeMatchers.pathMatchers("/login").matches(exchange)
								.filter(matchedPath -> matchedPath.isMatch()).flatMap(filteredRequest -> {
									return decryptLoginFilteredRequest(request);
								}).flatMap(changedRequest -> {
									return Mono.defer(() -> next.handle(changedRequest));
								}).switchIfEmpty(Mono.defer(() -> next.handle(request)));
					}
					return Mono.defer(() -> next.handle(request));
				}));
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		DispatcherHandler handler = (DispatcherHandler) ((DefaultWebFilterChain) chain).getHandler();
		if (handler.getHandlerMappings() != null) {
			Flux.fromIterable(handler.getHandlerMappings()).concatMap(mapping -> mapping.getHandler(exchange))
					.filter(handlerObject -> {
						return !handlerObject.getClass().getName().startsWith(LoginRouter.class.getPackageName());
					}).next()
					.then(Mono.defer(() -> ServerWebExchangeMatchers
							.pathMatchers("/", "/*.js", "/*.js.map", "/*.ico", "/getSession").matches(exchange)
							.flatMap(matcher -> {
								if (!matcher.isMatch()) {
									return ServerWebExchangeMatchers.pathMatchers("/login").matches(exchange)
											.filter(matchedPath -> matchedPath.isMatch()).flatMap(filteredRequest -> {
												return decryptLoginFilteredRequest(exchange);
											}).flatMap(changedExchange -> {
												return Mono.defer(() -> chain.filter(exchange.mutate()
														.request(changedExchange.getRequest()).build()));
											}).switchIfEmpty(Mono.defer(() -> chain.filter(exchange)));
								}
								return Mono.defer(() -> chain.filter(exchange));
							})));
		}
		return chain.filter(exchange);
	}

	private Mono<ServerWebExchange> decryptLoginFilteredRequest(ServerWebExchange exchange) {
		return Mono.just(exchange).flatMap((exchangeToMutate) -> {
			return exchangeToMutate.getSession();
		}).map(session -> {
			return (KeyPair) session.getAttributes().get(ENCRYPTION_KEY);
		}).zipWhen(rsaKey -> {
			ServerRequest filteredRequest = ServerRequest.create(exchange, codecConfigurer.getReaders());
			return filteredRequest.bodyToMono(String.class);
		}, (rsaKeyCombinator, encryptedString) -> {
			return Tuples.of(rsaKeyCombinator, Hex.decode(encryptedString));
		}).flatMap(byteArrayRSATuple -> {
			return this.rsaService.decrypt(byteArrayRSATuple.getT2(), byteArrayRSATuple.getT1());
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
			return (KeyPair) session.getAttributes().get(ENCRYPTION_KEY);
		}).zipWhen(rsaKey -> {
			return request.bodyToMono(String.class);
		}, (rsaKeyCombinator, encryptedString) -> {
			return Tuples.of(rsaKeyCombinator, Hex.decode(encryptedString));
		}).flatMap(byteArrayRSATuple -> {
			return this.rsaService.decrypt(byteArrayRSATuple.getT2(), byteArrayRSATuple.getT1());
		}).map(decryptedBytes -> {
			return ServerRequest.from(request).headers(header -> {
				header.setContentType(MediaType.APPLICATION_JSON);
			}).body(new String(decryptedBytes, StandardCharsets.UTF_8)).build();
		});
	}

}
