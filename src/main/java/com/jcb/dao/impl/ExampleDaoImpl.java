package com.jcb.dao.impl;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.jcb.dao.ExampleDao;
import com.jcb.dto.ExampleDto;
import com.jcb.enumeration.Gender;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import javax.annotation.PostConstruct;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class ExampleDaoImpl extends AbstractDaoImpl<ExampleDto> implements ExampleDao {

    private ExampleDaoImpl(ReactiveRedisConnectionFactory redisConnectionFactory,
	    ReactiveRedisOperations<String, ExampleDto> daoRedisOps, CompletionStage<CqlSession> cassandraSession,
	    Map<String, BoundStatement> boundStatementMap, BatchStatementBuilder batchStatementBuilder) {
	super(redisConnectionFactory, daoRedisOps, cassandraSession, boundStatementMap, batchStatementBuilder,
		ExampleDto.class);
    }

    @PostConstruct
    public void loadData() {
	redisConnectionFactory.getReactiveConnection().serverCommands().flushAll();
	redisConnectionFactory.getReactiveConnection().serverCommands().getClientName()
		.thenMany(Flux.just(1, 2, 3).map(name -> {
		    return ExampleDto.builder().id(name).firstName("Jeffry").middleName("Jacob").lastName("D ")
			    .dateOfBirth(LocalDate.now()).gender(Gender.MALE).build();
		}).flatMap(this::insert)).thenMany(daoRedisOps.opsForSet().members(ExampleDto.class.getSimpleName()))
		.subscribe(System.out::println);
    }

}
