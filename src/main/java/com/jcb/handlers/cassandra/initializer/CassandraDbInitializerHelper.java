package com.jcb.handlers.cassandra.initializer;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.detach.AttachmentPoint;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTableStart;
import com.datastax.oss.driver.internal.core.cql.DefaultColumnDefinitions;
import com.jcb.annotation.CassandraTable;
import com.jcb.annotation.ClusteringKeyColumn;
import com.jcb.annotation.PartitionKeyColumn;
import com.jcb.constants.SystemPropertyConstants;
import com.jcb.utility.UtilityMethodsHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import reactor.core.publisher.Mono;

public class CassandraDbInitializerHelper {

    private static Map<Class<?>, DataType> classMap = new HashMap<Class<?>, DataType>() {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8292403759892366875L;
	{
	    put(String.class, DataTypes.TEXT);
	    put(Integer.class, DataTypes.INT);
	    put(Long.class, DataTypes.BIGINT);
	    put(LocalDate.class, DataTypes.DATE);
	    put(Float.class, DataTypes.FLOAT);
	    put(UUID.class, DataTypes.UUID);
	    put(LocalTime.class, DataTypes.TIME);
	    put(ZonedDateTime.class, DataTypes.TIMESTAMP);
	    put(Double.class, DataTypes.DOUBLE);
	    put(InetSocketAddress.class, DataTypes.INET);
	}

    };

    public static class TableMetaDataHolder {

	public Map<String, DataType> columnMap = new HashMap<>();

	public Map<String, GenericType<?>> genericTypeMap = new HashMap<>();

	public Map<Integer, String> orderedPartitionKey = new HashMap<>();

	public Map<Integer, String> orderedClusterKey = new HashMap<>();

	public boolean isCreated = false;

	ColumnDefinitions tableColumns;

    }

    public static Boolean keySpaceInitialized = false;

    private static CreateTable createTable;

    public static boolean initializeKeySpace(CqlSession cassandraSession) throws ClassNotFoundException, IOException {
	String[] defaultKeyspaces = { "system_auth", "system_schema", "system_distributed", "system", "system_traces" };
	Set<String> keySpacesInPackage = getKeySpaceInPackage();
	List<String> keySpacesInCassandra = getKeySpacesInCassandra(cassandraSession);

	keySpacesInPackage.stream().filter(keySpace -> {
	    return (SystemPropertyConstants.CREATE_KEYSPACE) && (keySpacesInCassandra.indexOf(keySpace) == -1);
	}).forEach(keySpace -> {
	    createKeySpace(keySpace, cassandraSession).block();
	});

	return true;
    }

    private static Mono<ReactiveRow> createKeySpace(String keySpace, CqlSession cassandraSession) {
	return Mono.from(
		cassandraSession.executeReactive(SchemaBuilder.createKeyspace(keySpace).withSimpleStrategy(1).asCql()));
    }

    private static Set<String> getKeySpaceInPackage() throws ClassNotFoundException, IOException {
	Set<String> keyspacesInPackage = new HashSet<String>();
	List<Class<?>> dtoClasses = UtilityMethodsHelper.getAnnotatedClassesInPackage("com.jcb.dto",
		CassandraTable.class);
	dtoClasses.stream().forEach(clazz -> {
	    CassandraTable cassandraTable = clazz.getDeclaredAnnotation(CassandraTable.class);
	    keyspacesInPackage.add(cassandraTable.keySpace());

	});
	return keyspacesInPackage;
    }

    private static List<String> getKeySpacesInCassandra(CqlSession cassandraSession) {
	List<String> keySpacesInCassandra = new ArrayList<String>();
	try {
	    cassandraSession.executeAsync(SimpleStatement.builder("SELECT * FROM system_schema.keyspaces").build())
		    .whenComplete((object, throwable) -> {
			if (throwable != null) {
			    throwable.printStackTrace();
			    keySpaceInitialized = false;
			} else if (object instanceof RuntimeException) {
			    keySpaceInitialized = false;
			} else {
			    AsyncResultSet result = (AsyncResultSet) object;
			    result.currentPage().forEach(row -> {
				keySpacesInCassandra.add(row.get("keyspace_name", String.class));
			    });
			    keySpaceInitialized = true;
			}
		    }).toCompletableFuture().get();
	} catch (InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
	return keySpacesInCassandra;
    }

    public static CqlSession initializeCassandraSession(CompletionStage<CqlSession> cassandraSessionCompletionStage)
	    throws InterruptedException, ExecutionException {
	return cassandraSessionCompletionStage.handle((cqlSession, throwable) -> {
	    if (throwable != null) {
		throwable.printStackTrace();
		throw new RuntimeException(throwable);
	    }
	    return cqlSession;
	}).toCompletableFuture().get();
    }

    public static TableMetaDataHolder initializeTableMetaData(CqlSession cassandraSession, Class<?> dtoClass) {
	TableMetaDataHolder tableDataHolder = new TableMetaDataHolder();
	CassandraTable cassandraTable = dtoClass.getAnnotation(CassandraTable.class);
	String tableName = cassandraTable.tableName();
	String keySpaceName = cassandraTable.keySpace();
	Field[] dtoFields = dtoClass.getDeclaredFields();
	List<ColumnDefinition> columnDefinitionList = new ArrayList<>();
	for (Field dtoField : dtoFields) {
	    tableDataHolder.genericTypeMap.put(dtoField.getName(), GenericType.of(dtoField.getType()));
	    if (classMap.containsKey(dtoField.getType())) {
		columnDefinitionList.add(new ColumnDefinition() {

		    @Override
		    public boolean isDetached() {
			return false;

		    }

		    @Override
		    public void attach(AttachmentPoint attachmentPoint) {
		    }

		    @Override
		    public CqlIdentifier getKeyspace() {
			return CqlIdentifier.fromInternal(keySpaceName);
		    }

		    @Override
		    public CqlIdentifier getTable() {
			return CqlIdentifier.fromInternal(tableName);
		    }

		    @Override
		    public CqlIdentifier getName() {
			return CqlIdentifier.fromInternal(dtoField.getName());
		    }

		    @Override
		    public DataType getType() {
			return classMap.get(dtoField.getType());
		    }

		});
		tableDataHolder.columnMap.put(dtoField.getName(), classMap.get(dtoField.getType()));
	    } else {
		tableDataHolder.columnMap.put(dtoField.getName(), DataTypes.BLOB);
		columnDefinitionList.add(new ColumnDefinition() {

		    @Override
		    public boolean isDetached() {
			return false;

		    }

		    @Override
		    public void attach(AttachmentPoint attachmentPoint) {
		    }

		    @Override
		    public CqlIdentifier getKeyspace() {
			return CqlIdentifier.fromInternal(keySpaceName);
		    }

		    @Override
		    public CqlIdentifier getTable() {
			return CqlIdentifier.fromInternal(tableName);
		    }

		    @Override
		    public CqlIdentifier getName() {
			return CqlIdentifier.fromInternal(dtoField.getName());
		    }

		    @Override
		    public DataType getType() {
			return DataTypes.BLOB;
		    }

		});
	    }
	    PartitionKeyColumn primaryKeyField = dtoField.getAnnotation(PartitionKeyColumn.class);
	    ClusteringKeyColumn clusteringKeyField = dtoField.getAnnotation(ClusteringKeyColumn.class);
	    if (primaryKeyField != null) {
		tableDataHolder.orderedPartitionKey.put(primaryKeyField.value(), dtoField.getName());
	    } else if (clusteringKeyField != null) {
		tableDataHolder.orderedClusterKey.put(clusteringKeyField.value(), dtoField.getName());
	    }
	}
	tableDataHolder.tableColumns = DefaultColumnDefinitions.valueOf(columnDefinitionList);
	return tableDataHolder;
    }

    public static boolean createTable(TableMetaDataHolder tableData, CqlSession cassandraSession) {
	CreateTableStart createTableStart = SchemaBuilder
		.createTable(tableData.tableColumns.get(0).getKeyspace(), tableData.tableColumns.get(0).getTable())
		.ifNotExists();
	tableData.orderedPartitionKey.keySet().stream().sorted().forEach(partitionKeyOrder -> {
	    String partitionKeyName = tableData.orderedPartitionKey.get(partitionKeyOrder);
	    if (partitionKeyOrder == 0) {
		createTable = createTableStart.withPartitionKey(CqlIdentifier.fromInternal(partitionKeyName),
			tableData.columnMap.get(partitionKeyName));
	    }
	    createTable = createTableStart.withPartitionKey(CqlIdentifier.fromInternal(partitionKeyName),
		    tableData.columnMap.get(partitionKeyName));
	});
	tableData.orderedClusterKey.keySet().stream().sorted().forEach(clusterKeyOrder -> {
	    String clusterKeyName = tableData.orderedClusterKey.get(clusterKeyOrder);
	    createTable = createTable.withClusteringColumn(CqlIdentifier.fromInternal(clusterKeyName),
		    tableData.columnMap.get(clusterKeyName));
	});
	tableData.columnMap.forEach((columnName, dataType) -> {
	    if ((!tableData.orderedPartitionKey.containsValue(columnName))
		    && (!tableData.orderedClusterKey.containsValue(columnName))) {
		createTable = createTable.withColumn(CqlIdentifier.fromInternal(columnName), dataType);
	    }
	});
	ReactiveRow row = Mono.from(cassandraSession.executeReactive(createTable.asCql())).block();
	return true;
    }

}
