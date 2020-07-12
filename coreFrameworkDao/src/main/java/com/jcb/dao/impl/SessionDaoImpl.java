package com.jcb.dao.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jcb.dao.SessionDao;
import com.jcb.dto.SessionDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SessionDaoImpl extends AbstractDaoImpl<SessionDto> implements SessionDao {

	@Override
	public Flux<SessionDto> getSession(UUID sessionId) {
		Map<String, Object> partitionKeyMap = new HashMap<>();
		partitionKeyMap.put("sessionId", sessionId);
		return super.getForPartitionKey(partitionKeyMap);
	}

	@Override
	public Mono<SessionDto> getSession(UUID sessionId, String ipAddress, String userName) {
		return null;
	}

	@Override
	public Flux<SessionDto> getAll() {
		return super.getAll();
	}
}