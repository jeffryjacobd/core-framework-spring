package com.jcb.handlers.cassandra.helper;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.jcb.handlers.cassandra.initializer.CassandraDbInitializerHelper.TableMetaDataHolder;

import java.lang.reflect.Field;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class CassandraQueryHelperUtility {

    @Autowired
    private CqlSession cassandraSession;

    private RegularInsert insertStatement;

    public PreparedStatement createInsertPreparedStatement(TableMetaDataHolder tableData) {
	insertStatement = QueryBuilder
		.insertInto(tableData.tableColumns.get(0).getKeyspace(), tableData.tableColumns.get(0).getTable())
		.value(tableData.tableColumns.get(0).getName(),
			QueryBuilder.bindMarker(tableData.tableColumns.get(0).getName()));
	tableData.tableColumns.forEach(columnDefinition -> {
	    CqlIdentifier columnName = columnDefinition.getName();
	    insertStatement = insertStatement.value(columnName, QueryBuilder.bindMarker(columnName));
	});
	return cassandraSession.prepare(insertStatement.build());
    }

    @SuppressWarnings("unchecked")
    public <DtoName> BoundStatement bindValuesToBoundStatement(DtoName data, BoundStatement boundStatement,
	    TableMetaDataHolder tableData, Class<DtoName> dtoClass) {

	try {
	    for (Field dtoField : dtoClass.getDeclaredFields()) {
		dtoField.setAccessible(true);
		boundStatement = boundStatement.<Object>set(CqlIdentifier.fromInternal(dtoField.getName()),
			dtoField.get(data), tableData.genericTypeMap.get(dtoField.getName()));
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

    public <DtoName> Flux<DtoName> mapReactiveResultSetToDto(ReactiveResultSet asyncResult, Class<DtoName> dtoClass) {
	return Flux.from(asyncResult).map(reactiveRow -> {
	    return mapReactiveResultToDto(reactiveRow, dtoClass);
	});
    }

    @SuppressWarnings("unchecked")
    private <DtoName> DtoName mapReactiveResultToDto(ReactiveRow reactiveRow, Class<DtoName> dtoClass) {
	Object dto = null;
	try {
	    dto = dtoClass.getDeclaredConstructor().newInstance();
	    for (ColumnDefinition resultColumnDefinition : reactiveRow.getColumnDefinitions()) {
		String fieldName = resultColumnDefinition.getName().asInternal();
		Field dtoField = dtoClass.getDeclaredField(fieldName);
		dtoField.setAccessible(true);
		dtoField.set(dto,
			reactiveRow.get(resultColumnDefinition.getName(), GenericType.of(dtoField.getType())));
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
		throw new RuntimeException("The specified column does not exist in table");
	    });
	    specificColumnIdentifiers[specificColumnIdentifierIndex++] = CqlIdentifier.fromInternal(specificColumn);
	}
	return QueryBuilder
		.selectFrom(tableData.tableColumns.get(0).getKeyspace(), tableData.tableColumns.get(0).getTable())
		.columns(specificColumnIdentifiers).build();

    }

}
