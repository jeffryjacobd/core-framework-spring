package com.jcb.dao.impl;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BoundStatement;

import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public abstract class AbstractDaoImpl<DtoName> {

    protected final ReactiveRedisConnectionFactory redisConnectionFactory;

    protected final ReactiveRedisOperations<String, DtoName> daoRedisOps;

    protected final CompletionStage<CqlSession> cassandraSession;

    protected final Map<String, BoundStatement> boundStatementMap;

    protected final BatchStatementBuilder batchStatementbuilder;

    protected final Class<DtoName> dtoClass;

    public Flux<Long> insert(DtoName data) {
	return null;
    }

}
