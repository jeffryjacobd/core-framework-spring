package com.jcb.entity;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.jcb.dto.SessionDto;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

public class WebSession implements org.springframework.web.server.WebSession {

	public transient static final String DEFAULT_SESSION_HEADER = "X-Auth-Token";

	public transient static final String USER_NAME_KEY = "user-name-key";

	public transient static final String IP_KEY = "ip-address-key";

	public transient static final String USER_AGENT = "User-Agent";

	@Getter
	@Setter
	private String id = UUID.randomUUID().toString();

	@Getter
	@Setter
	private Map<String, Object> attributes = new HashMap<>();

	@Getter
	@Setter
	private boolean started;

	@Getter
	@Setter
	private boolean expired;

	@Override
	public void start() {
		this.started = true;
	}

	@Override
	public Mono<Void> changeSessionId() {
		this.id = UUID.randomUUID().toString();
		return Mono.empty();
	}

	@Getter
	@Setter
	private Instant creationTime;

	@Getter
	@Setter
	private Instant lastAccessTime;

	@Getter
	@Setter
	private Duration maxIdleTime;

	@Override
	public Mono<Void> invalidate() {
		this.expired = true;
		this.started = false;
		return Mono.empty();
	}

	@Override
	public Mono<Void> save() {
		this.lastAccessTime = Instant.now();
		return Mono.empty();
	}

	public static WebSession convertToEntity(SessionDto sessionDto) {
		WebSession session = new WebSession();
		session.setId(sessionDto.getSessionId().toString());
		session.setLastAccessTime(sessionDto.getLastAccessTime());
		session.setCreationTime(sessionDto.getCreationTime());
		session.setMaxIdleTime(sessionDto.getMaxIdleTime());
		session.setStarted(sessionDto.isStarted());
		session.setExpired(sessionDto.isExpired());
		session.setAttributes(sessionDto.getAttributes());
		return session;
	}

	public SessionDto convertToDto() {
		SessionDto sessionDto = new SessionDto();
		sessionDto.setLastAccessTime(this.getLastAccessTime());
		sessionDto.setAttributes(this.getAttributes());
		sessionDto.setIpAddress(this.attributes.get(IP_KEY).toString());
		sessionDto.setUserName(this.getAttributes().get(USER_NAME_KEY).toString());
		sessionDto.setSessionId(UUID.fromString(this.getId()));
		return sessionDto;
	}

}
