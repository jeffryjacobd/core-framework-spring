package com.jcb.dao.impl;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.jcb.handlers.cassandra.helper.CassandraQueryHelperUtility;
import com.jcb.handlers.cassandra.initializer.CassandraDbInitializerHelper;
import com.jcb.handlers.cassandra.initializer.CassandraDbInitializerHelper.TableMetaDataHolder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;

import lombok.Setter;
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

    @Autowired
    private CassandraDbInitializerHelper cassandraDbInitializerHelper;

    private PreparedStatement insertPreparedStatement;

    private TableMetaDataHolder tableData;

    public Mono<Boolean> insert(DtoName data) {
	BoundStatement insertStatement = cassandraQueryHelperUtility.bindValuesToBoundStatement(data,
		insertPreparedStatement.bind(), tableData, dtoClass);
	return Mono.from(cassandraSession.executeReactive(insertStatement)).retry(2).map(reactiveRow -> {
	    return reactiveRow.getExecutionInfo() != null;
	});
    }

    @PostConstruct
    void initDBProcedure() throws ClassNotFoundException, IOException, InterruptedException, ExecutionException {
	CassandraDbInitializerHelper.keySpaceInitialized = (!cassandraDbInitializerHelper.keySpaceInitialized)
		? cassandraDbInitializerHelper.initializeKeySpace()
		: true;
	tableData = cassandraDbInitializerHelper.initializeTableMetaData(dtoClass);
	tableData.isCreated = (!tableData.isCreated) ? cassandraDbInitializerHelper.createTable(tableData) : true;
	insertPreparedStatement = (insertPreparedStatement != null) ? insertPreparedStatement
		: cassandraQueryHelperUtility.createInsertPreparedStatement(tableData);
	boundStatementMap.put(insertPreparedStatement.getQuery(), insertPreparedStatement.bind());

    }

}
