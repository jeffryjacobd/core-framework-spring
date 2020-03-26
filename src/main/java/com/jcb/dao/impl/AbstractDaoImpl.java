package com.jcb.dao.impl;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.jcb.handlers.cassandra.helper.CassandraQueryHelperUtility;
import com.jcb.handlers.cassandra.initializer.CassandraDbInitializerHelper.TableMetaDataHolder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;

import lombok.Setter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class AbstractDaoImpl<DtoName> {

    @Setter
    protected static ReactiveRedisConnectionFactory redisConnectionFactory;

    @Setter
    protected ReactiveRedisOperations<String, DtoName> daoRedisOps;

    @Setter
    private static CqlSession cassandraSession;

    @Setter
    protected static Map<String, BoundStatement> boundStatementMap;

    @Setter
    protected static BatchStatementBuilder batchStatementbuilder;

    @Setter
    private Class<DtoName> dtoClass;

    @Autowired
    private CassandraQueryHelperUtility cassandraQueryHelperUtility;

    private PreparedStatement insertPreparedStatement;

    private PreparedStatement selectAllPreparedStatement;

    @Setter
    private TableMetaDataHolder tableMetaData;

    public Mono<Boolean> insert(DtoName data) {
	BoundStatement insertStatement = cassandraQueryHelperUtility.bindValuesToBoundStatement(data,
		boundStatementMap.get(insertPreparedStatement.getQuery()), tableMetaData, dtoClass);
	return Mono.from(cassandraSession.executeReactive(insertStatement)).retry(2).map(reactiveRow -> {
	    return reactiveRow.getExecutionInfo() != null;
	});
    }

    public Flux<DtoName> getAll(String... specificColumns) {
	if (specificColumns.length != 0) {
	    return cassandraQueryHelperUtility.mapReactiveResultSetToDto(
		    cassandraSession.executeReactive(
			    cassandraQueryHelperUtility.createSelectSpecificStatement(tableMetaData, specificColumns)),
		    dtoClass);
	}
	return cassandraQueryHelperUtility.mapReactiveResultSetToDto(
		cassandraSession.executeReactive(boundStatementMap.get(selectAllPreparedStatement.getQuery())),
		dtoClass);
    }

    @PostConstruct
    void initDBProcedure() throws ClassNotFoundException, IOException, InterruptedException, ExecutionException {
	insertPreparedStatement = (insertPreparedStatement != null) ? insertPreparedStatement
		: cassandraQueryHelperUtility.createInsertPreparedStatement(tableMetaData);
	boundStatementMap.put(insertPreparedStatement.getQuery(), insertPreparedStatement.bind());
	selectAllPreparedStatement = (selectAllPreparedStatement != null) ? selectAllPreparedStatement
		: cassandraQueryHelperUtility.createSelectAllPreparedStatement(tableMetaData);
	boundStatementMap.put(selectAllPreparedStatement.getQuery(), selectAllPreparedStatement.bind());

    }

}
