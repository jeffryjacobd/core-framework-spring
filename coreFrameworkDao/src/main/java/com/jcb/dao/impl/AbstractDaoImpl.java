package com.jcb.dao.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.jcb.handlers.cassandra.helper.CassandraQueryHelperUtility;
import com.jcb.handlers.cassandra.initializer.CassandraDbInitializerHelper.TableMetaDataHolder;

import ch.qos.logback.classic.Logger;
import lombok.Setter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class AbstractDaoImpl<DtoName> {

	private final Logger LOG;
	{
		LOG = (Logger) LoggerFactory.getLogger(this.getClass());
	}

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
		LOG.info("Insert data into table {}", dtoClass.getName());
		return Mono.from(cassandraSession.executeReactive(insertStatement)).map(reactiveRow -> {
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
