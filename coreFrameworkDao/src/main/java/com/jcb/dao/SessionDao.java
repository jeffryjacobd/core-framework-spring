package com.jcb.dao;

import java.util.UUID;

import com.jcb.dto.SessionDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SessionDao {

	Flux<SessionDto> getSession(UUID sessionId);

	Mono<SessionDto> getSession(UUID sessionId, String ipAddress, String userName);

	public Mono<Boolean> insert(SessionDto data);

	public Flux<SessionDto> getAll();

}
