package com.jcb.handlers.cassandra.helper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.jcb.handlers.cassandra.initializer.CassandraDbInitializerHelper.TableMetaDataHolder;

import ch.qos.logback.classic.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CassandraQueryHelperUtility {

	private static final Logger LOG = (Logger) LoggerFactory.getLogger(CassandraQueryHelperUtility.class);

	@Autowired
	private CqlSession cassandraSession;

	private RegularInsert insertStatement;

	private volatile CompletionStage<AsyncResultSet> currentAsyncResult;

	private volatile CompletionStage<AsyncResultSet> previousAsyncResult;

	public PreparedStatement createInsertPreparedStatement(TableMetaDataHolder tableData) {
		CqlIdentifier keySpace = tableData.tableColumns.get(0).getKeyspace();
		CqlIdentifier tableName = tableData.tableColumns.get(0).getTable();
		insertStatement = QueryBuilder.insertInto(keySpace, tableName).value(tableData.tableColumns.get(0).getName(),
				QueryBuilder.bindMarker(tableData.tableColumns.get(0).getName()));
		tableData.tableColumns.forEach(columnDefinition -> {
			CqlIdentifier columnName = columnDefinition.getName();
			insertStatement = insertStatement.value(columnName, QueryBuilder.bindMarker(columnName));
		});
		LOG.debug("Created prepare statement for insert query for table {} in keyspace {}", tableName.asInternal(),
				keySpace.asInternal());
		return cassandraSession.prepare(insertStatement.build());
	}

	@SuppressWarnings("unchecked")
	public <DtoName> BoundStatement bindValuesToBoundStatement(DtoName data, BoundStatement boundStatement,
			TableMetaDataHolder tableData, Class<DtoName> dtoClass) {

		try {
			for (Field dtoField : dtoClass.getDeclaredFields()) {
				dtoField.setAccessible(true);
				Object value = dtoField.get(data);
				if (value != null) {
					boundStatement = boundStatement.<Object>set(CqlIdentifier.fromInternal(dtoField.getName()), value,
							tableData.genericTypeMap.get(dtoField.getName()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return boundStatement;

	}

	public PreparedStatement createSelectAllPreparedStatement(TableMetaDataHolder tableData) {
		return cassandraSession.prepare(QueryBuilder
				.selectFrom(tableData.tableColumns.get(0).getKeyspace(), tableData.tableColumns.get(0).getTable()).all()
				.build());

	}

	public PreparedStatement createSelectBasedOnPartitionPreparedStatement(TableMetaDataHolder tableData) {
		List<Relation> additionalRelations = tableData.orderedPartitionKey.entrySet().stream()
				.sorted((entry1, entry2) -> {
					return (entry1.getKey() - entry2.getKey());
				}).map(partitionKey -> {
					return CqlIdentifier.fromInternal(partitionKey.getValue());
				}).map(cqlIdentifier -> {
					return Relation.column(cqlIdentifier).isEqualTo(QueryBuilder.bindMarker());
				}).collect(Collectors.toList());
		return cassandraSession.prepare(QueryBuilder
				.selectFrom(tableData.tableColumns.get(0).getKeyspace(), tableData.tableColumns.get(0).getTable()).all()
				.where(additionalRelations).build());
	}

	public synchronized <DtoName> Flux<DtoName> mapReactiveResultSetToDto(CompletionStage<AsyncResultSet> asyncResult,
			Class<DtoName> dtoClass) {
		this.currentAsyncResult = asyncResult;
		return Mono.defer(() -> {
			return Mono.fromCompletionStage(this.currentAsyncResult).map(result -> {
				if (result.hasMorePages()) {
					fetchNextPage(result, asyncResult);
				} else {
					this.previousAsyncResult = this.currentAsyncResult;
				}
				return result;
			});
		}).repeat(this::canRepeatForPaging).map(resultSet -> {
			return resultSet.currentPage();
		}).flatMap(iterableRow -> {
			return Flux.fromIterable(iterableRow);
		}).map(row -> {
			return mapReactiveResultToDto(row, dtoClass);
		});
	}

	private void fetchNextPage(AsyncResultSet resultSet, CompletionStage<AsyncResultSet> asyncResult) {
		this.currentAsyncResult = resultSet.fetchNextPage();
		this.previousAsyncResult = asyncResult;
	}

	private boolean canRepeatForPaging() {
		return (this.currentAsyncResult != this.previousAsyncResult);
	}

	@SuppressWarnings("unchecked")
	private <DtoName> DtoName mapReactiveResultToDto(Row reactiveRow, Class<DtoName> dtoClass) {
		Object dto = null;
		try {
			dto = dtoClass.getDeclaredConstructor().newInstance();
			for (ColumnDefinition resultColumnDefinition : reactiveRow.getColumnDefinitions()) {
				String fieldName = resultColumnDefinition.getName().asInternal();
				Field dtoField = dtoClass.getDeclaredField(fieldName);
				dtoField.setAccessible(true);
				Object value = null;
				try {
					value = reactiveRow.get(resultColumnDefinition.getName(), GenericType.of(dtoField.getType()));
				} catch (NullPointerException e) {
				}
				dtoField.set(dto, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return (DtoName) dto;
	}

	public SimpleStatement createSelectSpecificStatement(TableMetaDataHolder tableData, String[] specificColumns) {
		CqlIdentifier[] specificColumnIdentifiers = new CqlIdentifier[specificColumns.length];
		int specificColumnIdentifierIndex = 0;
		for (String specificColumn : specificColumns) {
			tableData.columnMap.computeIfAbsent(specificColumn, key -> {
				LOG.error("The specified column: {}, does not exist in table", specificColumn);
				throw new RuntimeException("The specified column does not exist in table");
			});
			specificColumnIdentifiers[specificColumnIdentifierIndex++] = CqlIdentifier.fromInternal(specificColumn);
		}
		return QueryBuilder
				.selectFrom(tableData.tableColumns.get(0).getKeyspace(), tableData.tableColumns.get(0).getTable())
				.columns(specificColumnIdentifiers).build();

	}

	public BoundStatement bindSelectBindMarkers(BoundStatement boundStatement, Map<String, Object> partitionKeyMap,
			TableMetaDataHolder tableMeta) {
		Object[] whereData = tableMeta.orderedPartitionKey.entrySet().stream().sorted((entry1, entry2) -> {
			return (entry1.getKey() - entry2.getKey());
		}).map(entry -> {
			return partitionKeyMap.get(entry.getValue());
		}).collect(Collectors.toList()).toArray();
		return boundStatement.getPreparedStatement().bind(whereData);
	}

}
