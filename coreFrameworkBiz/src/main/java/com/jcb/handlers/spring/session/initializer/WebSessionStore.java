package com.jcb.handlers.spring.session.initializer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebSession;

import com.jcb.dao.SessionDao;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class WebSessionStore implements org.springframework.web.server.session.WebSessionStore {

	@Autowired
	private SessionDao sessionDao;

	private static SessionDao staticSessionDao;

	private static final int SESSION_CACHE_LIMIT = 100;

	public static final Map<String, WebSession> sessionMapFifoCache = new LinkedHashMap<>() {
		private static final long serialVersionUID = 2533226104210001369L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, WebSession> entry) {
			if (size() > SESSION_CACHE_LIMIT) {
				com.jcb.entity.WebSession session = (com.jcb.entity.WebSession) entry.getValue();
				staticSessionDao.insert(session.convertToDto()).subscribeOn(Schedulers.boundedElastic()).subscribe();
				return true;
			}
			return false;
		}
	};

	@Override
	public Mono<WebSession> createWebSession() {
		return Mono.just(new com.jcb.entity.WebSession()).zipWhen((webSession) -> {
			return sessionDao.insert(webSession.convertToDto());
		}, (webSession, insertStatus) -> {
			return webSession;
		});
	}

	@Override
	public Mono<WebSession> retrieveSession(String sessionId) {
		return Mono.justOrEmpty(sessionMapFifoCache.get(sessionId)).switchIfEmpty(
				Mono.defer(() -> sessionDao.getSession(UUID.fromString(sessionId)).next().map(sessionDto -> {
					WebSession session = com.jcb.entity.WebSession.convertToEntity(sessionDto);
					sessionMapFifoCache.put(sessionId, session);
					return session;
				})));
	}

	@Override
	public Mono<Void> removeSession(String sessionId) {
		sessionMapFifoCache.remove(sessionId);
		// TO DO Db delete
		return Mono.empty();
	}

	@Override
	public Mono<WebSession> updateLastAccessTime(WebSession webSession) {
		com.jcb.entity.WebSession session = (com.jcb.entity.WebSession) webSession;
		session.save();
		sessionMapFifoCache.put(session.getId(), session);
		return Mono.just(session);
	}

	@PostConstruct
	void initalize() {
		staticSessionDao = sessionDao;
	}

}
