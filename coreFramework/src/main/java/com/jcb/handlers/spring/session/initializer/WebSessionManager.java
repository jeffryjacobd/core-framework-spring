package com.jcb.handlers.spring.session.initializer;

import static com.jcb.entity.WebSession.IP_KEY;
import static com.jcb.entity.WebSession.USER_NAME_KEY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.session.WebSessionIdResolver;
import org.springframework.web.server.session.WebSessionStore;

import com.jcb.dao.SessionDao;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component("webSessionManager")
public class WebSessionManager implements org.springframework.web.server.session.WebSessionManager {

	@Autowired
	private WebSessionIdResolver sessionIdResolver;

	@Autowired
	private WebSessionStore sessionStore;

	@Autowired
	private SessionDao sessionDao;

	@Override
	public Mono<WebSession> getSession(ServerWebExchange exchange) {
		return Mono.defer(() -> retrieveSession(exchange).switchIfEmpty(Mono.defer(() -> createWebSession(exchange)))
				.doOnNext(session -> exchange.getResponse().beforeCommit(() -> save(exchange, session))));
	}

	private Mono<Void> save(ServerWebExchange exchange, WebSession session) {
		return this.sessionStore.updateLastAccessTime(session).doOnNext(updatedSession -> {
			writeToResponse(session, updatedSession, exchange);
		}).then();
	}

	private void writeToResponse(WebSession requestSession, WebSession updatedSession, ServerWebExchange exchange) {
		if (ispublicFile(exchange)) {
			return;
		}
		this.sessionIdResolver.setSessionId(exchange, updatedSession.getId());
	}

	private Mono<? extends WebSession> createWebSession(ServerWebExchange exchange) {
		if (ispublicFile(exchange)) {
			return Mono.empty();
		}
		com.jcb.entity.WebSession session = new com.jcb.entity.WebSession();
		session.getAttributes().put(IP_KEY, exchange.getRequest().getRemoteAddress().getHostString());
		session.getAttributes().put(USER_NAME_KEY, "");
		return this.sessionDao.insert(session.convertToDto()).flatMap(insertResult -> {
			com.jcb.handlers.spring.session.initializer.WebSessionStore.sessionMapFifoCache.put(session.getId(),
					session);
			return Mono.just(session);
		});
	}

	private Mono<WebSession> retrieveSession(ServerWebExchange exchange) {
		if (this.ispublicFile(exchange)) {
			return Mono.empty();
		}
		return Flux.fromIterable(this.sessionIdResolver.resolveSessionIds(exchange)).next().flatMap(sessionId -> {
			if (sessionId.isEmpty()) {
				return Mono.empty();
			}
			return this.sessionStore.retrieveSession(sessionId);
		});

	}

	private boolean ispublicFile(ServerWebExchange exchange) {
		String requestPath = exchange.getRequest().getPath().toString();
		return (requestPath.endsWith(".js") || requestPath.endsWith(".js.map") || requestPath.endsWith(".ico")
				|| requestPath.equalsIgnoreCase("/"));
	}
}
