package com.jcb.handlers.cassandra.helper;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.RegularInsert;
import com.jcb.handlers.cassandra.initializer.CassandraDbInitializerHelper.TableMetaDataHolder;

import java.lang.reflect.Field;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

}
