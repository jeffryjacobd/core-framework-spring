package com.jcb.dao.impl;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;

@AllArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public abstract class AbstractDaoImpl<DtoName> {

    protected ReactiveRedisConnectionFactory redisConnectionFactory;

    protected ReactiveRedisOperations<String, DtoName> daoRedisOps;

    protected CompletionStage<CqlSession> cassandraSession;

    Map<String, PreparedStatement> preparedStatement;

    public Flux<Long> insert(DtoName data) {
	return null;
    }

}
