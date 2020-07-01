package com.jcb.config;

import static com.jcb.constants.SystemPropertyConstants.CASSANDRA_POINTS_PROPERTY;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.metadata.schema.SchemaChangeListener;
import com.datastax.oss.driver.api.core.type.codec.registry.MutableCodecRegistry;
import com.jcb.handlers.cassandra.listener.schemachange.CassandraSchemaChangeListener;

@Configuration
@EnableCaching
public class CassandraConfig {

	@Bean
	CqlSession cassandraSession() throws InterruptedException, ExecutionException {
		CompletionStage<CqlSession> cqlSession = mapContactPoints(CqlSession.builder());
		return cqlSession.toCompletableFuture().get();
	}

	@Bean
	MutableCodecRegistry mutableCodecRegistry(CqlSession cassandraSession) {
		return (MutableCodecRegistry) cassandraSession.getContext().getCodecRegistry();
	}

	private static CompletionStage<CqlSession> mapContactPoints(CqlSessionBuilder cqlSessionBuilder) {
		String cassandraPoints = System.getProperty(CASSANDRA_POINTS_PROPERTY);
		if (StringUtils.isEmpty(cassandraPoints)) {
			return cqlSessionBuilder.withKeyspace("system_schema").buildAsync();
		} else {
			for (String cassandraPoint : cassandraPoints.split(",")) {
				cqlSessionBuilder = cqlSessionBuilder.addContactPoint(InetSocketAddress.createUnresolved(
						cassandraPoint.substring(0, cassandraPoint.indexOf(":")).trim(), Integer.valueOf(cassandraPoint
								.substring(cassandraPoint.indexOf(":") + 1, cassandraPoint.length()).trim())));
			}
		}
		return cqlSessionBuilder.withSchemaChangeListener(getSchemaChangeListener()).buildAsync();
	}

	static SchemaChangeListener getSchemaChangeListener() {
		return new CassandraSchemaChangeListener();
	}

	@Bean("boundStatementMap")
	Map<String, BoundStatement> getPreparedStatementMap() {
		return new HashMap<String, BoundStatement>();
	}

	@Bean("batchStatementBuilder")
	BatchStatementBuilder getBatchStatement() {
		return new BatchStatementBuilder(DefaultBatchType.LOGGED);
	}

}
