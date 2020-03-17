package com.jcb.dao.impl;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.jcb.handlers.cassandra.initializer.CassandraDbInitializerHelper;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;

import lombok.Setter;
import reactor.core.publisher.Flux;

public abstract class AbstractDaoImpl<DtoName> {

    @Setter
    protected static ReactiveRedisConnectionFactory redisConnectionFactory;

    @Setter
    protected ReactiveRedisOperations<String, DtoName> daoRedisOps;

    @Setter
    private static CompletionStage<CqlSession> cassandraSessionCompletionStage;

    @Setter
    protected static Map<String, BoundStatement> boundStatementMap;

    @Setter
    protected static BatchStatementBuilder batchStatementbuilder;

    @Setter
    private Class<DtoName> dtoClass;

    protected static CqlSession cassandraSession;

    private Boolean tableInitialized = false;

    public Flux<Long> insert(DtoName data) {
	return null;
    }

    @PostConstruct
    void initDBProcedure() throws ClassNotFoundException, IOException, InterruptedException, ExecutionException {
	cassandraSession = (cassandraSession == null) ? initializeCassandraSession() : cassandraSession;
	CassandraDbInitializerHelper.keySpaceInitialized = (!CassandraDbInitializerHelper.keySpaceInitialized)
		? CassandraDbInitializerHelper.initializeKeySpace(cassandraSession)
		: true;
	tableInitialized = CassandraDbInitializerHelper.initializeTable(cassandraSession);
    }

    private static CqlSession initializeCassandraSession() throws InterruptedException, ExecutionException {
	return cassandraSessionCompletionStage.handle((cqlSession, throwable) -> {
	    if (throwable != null) {
		throwable.printStackTrace();
		throw new RuntimeException(throwable);
	    }
	    return cqlSession;
	}).toCompletableFuture().get();
    }

}

class TableMetaDataHolder {

}
