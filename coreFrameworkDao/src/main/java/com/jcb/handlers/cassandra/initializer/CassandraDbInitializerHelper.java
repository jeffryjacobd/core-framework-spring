package com.jcb.handlers.cassandra.initializer;

import static com.jcb.constants.SystemPropertyConstants.ALTER_COLUMN;
import static com.jcb.constants.SystemPropertyConstants.CREATE_COLUMN;
import static com.jcb.constants.SystemPropertyConstants.CREATE_TABLE;
import static com.jcb.constants.SystemPropertyConstants.DELETE_COLUMN;
import static com.jcb.constants.SystemPropertyConstants.DELETE_TABLE;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.detach.AttachmentPoint;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.registry.MutableCodecRegistry;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTableStart;
import com.datastax.oss.driver.api.querybuilder.schema.compaction.CompactionStrategy;
import com.datastax.oss.driver.internal.core.cql.DefaultColumnDefinitions;
import com.jcb.annotation.CassandraTable;
import com.jcb.annotation.ClusteringKeyColumn;
import com.jcb.annotation.PartitionKeyColumn;
import com.jcb.constants.SystemPropertyConstants;
import com.jcb.handlers.cassandra.codec.EnumTypeCodec;
import com.jcb.handlers.cassandra.codec.GenericTypeObjectCodec;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Logger;
import reactor.core.publisher.Mono;

@Component
public class CassandraDbInitializerHelper {

    private static final Logger LOG = (Logger) LoggerFactory.getLogger(CassandraDbInitializerHelper.class);

    @Autowired
    private CqlSession cassandraSession;

    @Autowired
    private MutableCodecRegistry mutableCodecRegistry;

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

	public Map<String, GenericType> genericTypeMap = new HashMap<>();

	public Map<Integer, String> orderedPartitionKey = new HashMap<>();

	public Map<Integer, String> orderedClusterKey = new HashMap<>();

	@SuppressWarnings("rawtypes")
	public CompactionStrategy compactionStratergy = SchemaBuilder.sizeTieredCompactionStrategy().withBucketHigh(1.5)
		.withBucketLow(0.5).withEnabled(true);

	public boolean isCreated = false;

	public ColumnDefinitions tableColumns;

	public int ttl;

    }

    private static Boolean keySpaceInitialized = false;

    private static CreateTable createTable;

    private boolean initializeKeySpace() throws ClassNotFoundException, IOException {
	@SuppressWarnings("unused")
	String[] defaultKeyspaces = { "system_auth", "system_schema", "system_distributed", "system", "system_traces" };
	Set<String> keySpacesInPackage = getKeySpaceInPackage();
	List<String> keySpacesInCassandra = getKeySpacesInCassandra();

	keySpacesInPackage.stream().filter(keySpace -> {
	    return (SystemPropertyConstants.CREATE_KEYSPACE) && (keySpacesInCassandra.indexOf(keySpace) == -1);
	}).forEach(keySpace -> {
	    createKeySpace(keySpace).block();
	    LOG.info("{} keyspace created", keySpace);
	});

	return true;
    }

    private Mono<ReactiveRow> createKeySpace(String keySpace) {
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

    private List<String> getKeySpacesInCassandra() {
	return cassandraSession.getMetadata().getKeyspaces().keySet().stream().map(keyspaceIdentifier -> {
	    return keyspaceIdentifier.asInternal();
	}).collect(Collectors.toList());
    }

    public TableMetaDataHolder initializeTableMetaData(Class<?> dtoClass) throws ClassNotFoundException, IOException {
	keySpaceInitialized = (keySpaceInitialized == false) ? initializeKeySpace() : keySpaceInitialized;
	TableMetaDataHolder tableDataHolder = new TableMetaDataHolder();
	CassandraTable cassandraTable = dtoClass.getAnnotation(CassandraTable.class);
	String tableName = cassandraTable.tableName();
	String keySpaceName = cassandraTable.keySpace();
	tableDataHolder.ttl = cassandraTable.ttl();
	Field[] dtoFields = dtoClass.getDeclaredFields();
	List<ColumnDefinition> columnDefinitionList = new ArrayList<>();
	for (Field dtoField : dtoFields) {
	    tableDataHolder.genericTypeMap.put(dtoField.getName(), GenericType.of(dtoField.getType()));
	    if (classMap.containsKey(dtoField.getType())) {
		columnDefinitionList.add(
			createColumnDefinition(keySpaceName, tableName, dtoField, classMap.get(dtoField.getType())));
		tableDataHolder.columnMap.put(dtoField.getName(), classMap.get(dtoField.getType()));
	    } else {
		if (dtoField.getType().isEnum()) {
		    tableDataHolder.columnMap.put(dtoField.getName(), DataTypes.TEXT);
		    columnDefinitionList.add(createColumnDefinition(keySpaceName, tableName, dtoField, DataTypes.TEXT));
		    mutableCodecRegistry.register(EnumTypeCodec.of(dtoField.getType(), DataTypes.TEXT));
//		} else if (dtoField.getType().isAssignableFrom(List.class)) {
//
//		} else if (dtoField.getType().isAssignableFrom(Set.class)) {
//
//		} else if (dtoField.getType().isAssignableFrom(Map.class)) {
//
//		} 
		} else {
		    tableDataHolder.columnMap.put(dtoField.getName(), DataTypes.BLOB);
		    columnDefinitionList.add(createColumnDefinition(keySpaceName, tableName, dtoField, DataTypes.BLOB));
		    mutableCodecRegistry.register(GenericTypeObjectCodec.of(dtoField.getType(), DataTypes.BLOB));
		}
	    }
	    PartitionKeyColumn primaryKeyField = dtoField.getAnnotation(PartitionKeyColumn.class);
	    ClusteringKeyColumn clusteringKeyField = dtoField.getAnnotation(ClusteringKeyColumn.class);
	    if (primaryKeyField != null) {
		tableDataHolder.orderedPartitionKey.put(primaryKeyField.value(), dtoField.getName());
	    }
	    if (clusteringKeyField != null) {
		tableDataHolder.orderedClusterKey.put(clusteringKeyField.value(),
			dtoField.getName() + "," + clusteringKeyField.clusteringOrder().name());
	    }
	}
	tableDataHolder.tableColumns = DefaultColumnDefinitions.valueOf(columnDefinitionList);
	TableMetadata tableMetaDataInDatabase = getTableMetaDataInKeyspace(keySpaceName, tableName).orElseGet(() -> {
	    if (CREATE_TABLE) {
		tableDataHolder.isCreated = createTable(tableDataHolder);
	    } else {
		throw new Error("A Missing table is not created");
	    }
	    return getTableMetaDataInKeyspace(keySpaceName, tableName).get();
	});
	checkForTableSchemaMatch(tableMetaDataInDatabase, tableDataHolder);
	return tableDataHolder;
    }

    private void checkForTableSchemaMatch(TableMetadata tableMetaDataInDatabase, TableMetaDataHolder tableDataHolder) {
	tableMetaDataInDatabase.getPartitionKey().forEach((columnMetaData) -> {
	    if (!(tableDataHolder.orderedPartitionKey.values().contains(columnMetaData.getName().asInternal())
		    && columnMetaData.getType()
			    .equals(tableDataHolder.columnMap.get(columnMetaData.getName().asInternal())))) {
		dropTable(tableDataHolder);
		createTable(tableDataHolder);
		return;
	    }
	});
	tableMetaDataInDatabase.getClusteringColumns().forEach((columnMetaData, clusteringOrder) -> {
	    if (!(tableDataHolder.orderedClusterKey.values()
		    .contains(columnMetaData.getName().asInternal() + "," + clusteringOrder.name())
		    && columnMetaData.getType()
			    .equals(tableDataHolder.columnMap.get(columnMetaData.getName().asInternal())))) {
		dropTable(tableDataHolder);
		createTable(tableDataHolder);
		return;
	    }
	});
	tableMetaDataInDatabase.getColumns().forEach((columnName, columnMetaData) -> {
	    if (tableDataHolder.columnMap.containsKey(columnName.asInternal())) {
		if (!columnMetaData.getType().equals(tableDataHolder.columnMap.get(columnName.asInternal()))) {
		    alterColumn(tableDataHolder.tableColumns.get(0).getKeyspace(),
			    tableDataHolder.tableColumns.get(0).getTable(), columnName,
			    tableDataHolder.columnMap.get(columnName.asInternal()));
		}
	    } else {
		deleteColumn(tableDataHolder.tableColumns.get(0).getKeyspace(),
			tableDataHolder.tableColumns.get(0).getTable(), columnName);
	    }
	});
	tableMetaDataInDatabase = getTableMetaDataInKeyspace(
		tableDataHolder.tableColumns.get(0).getKeyspace().asInternal(),
		tableDataHolder.tableColumns.get(0).getTable().asInternal()).get();
	Map<CqlIdentifier, ColumnMetadata> columnData = tableMetaDataInDatabase.getColumns();
	if (tableMetaDataInDatabase.getColumns().size() != tableDataHolder.columnMap.size()) {
	    tableDataHolder.columnMap.forEach((columnName, columnType) -> {
		if (dbMetadatacontainsKey(columnData, columnName)) {
		    createColumn(tableDataHolder.tableColumns.get(0).getKeyspace(),
			    tableDataHolder.tableColumns.get(0).getTable(), CqlIdentifier.fromInternal(columnName),
			    tableDataHolder.columnMap.get(columnName));
		}
	    });
	}
    }

    private boolean dbMetadatacontainsKey(Map<CqlIdentifier, ColumnMetadata> columnData, String columnName) {
	return !columnData.containsKey(CqlIdentifier.fromInternal(columnName));
    }

    private void deleteColumn(CqlIdentifier keyspace, CqlIdentifier tableName, CqlIdentifier columnName) {
	if (DELETE_COLUMN) {
	    cassandraSession.execute(SchemaBuilder.alterTable(keyspace, tableName).dropColumn(columnName).build());
	}
    }

    private void createColumn(CqlIdentifier keyspace, CqlIdentifier tableName, CqlIdentifier columnName,
	    DataType dataType) {
	if (CREATE_COLUMN) {
	    cassandraSession
		    .execute(SchemaBuilder.alterTable(keyspace, tableName).addColumn(columnName, dataType).build());
	}
    }

    private void alterColumn(CqlIdentifier keyspace, CqlIdentifier tableName, CqlIdentifier columnName,
	    DataType dataType) {
	if (ALTER_COLUMN) {
	    cassandraSession
		    .execute(SchemaBuilder.alterTable(keyspace, tableName).alterColumn(columnName, dataType).build());
	}
    }

    private void dropTable(TableMetaDataHolder tableDataHolder) {
	if (DELETE_TABLE) {
	    cassandraSession.execute(SchemaBuilder.dropTable(tableDataHolder.tableColumns.get(0).getKeyspace(),
		    tableDataHolder.tableColumns.get(0).getTable()).build());
	}
    }

    private Optional<TableMetadata> getTableMetaDataInKeyspace(String keySpaceName, String tableName) {
	return cassandraSession.getMetadata().getKeyspace(CqlIdentifier.fromInternal(keySpaceName)).get()
		.getTable(CqlIdentifier.fromInternal(tableName));
    }

    private static ColumnDefinition createColumnDefinition(String keySpaceName, String tableName, Field dtoField,
	    DataType dataType) {
	return new ColumnDefinition() {

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
		return dataType;
	    }

	};

    }

    private boolean createTable(TableMetaDataHolder tableData) {
	CqlIdentifier tableName = tableData.tableColumns.get(0).getTable();
	CqlIdentifier keySpaceName = tableData.tableColumns.get(0).getKeyspace();
	CreateTableStart createTableStart = SchemaBuilder.createTable(keySpaceName, tableName).ifNotExists();
	tableData.orderedPartitionKey.keySet().stream().sorted().forEach(partitionKeyOrder -> {
	    String partitionKeyName = tableData.orderedPartitionKey.get(partitionKeyOrder);
	    if (partitionKeyOrder == 0) {
		createTable = createTableStart.withPartitionKey(CqlIdentifier.fromInternal(partitionKeyName),
			tableData.columnMap.get(partitionKeyName));
	    }
	    createTable = createTableStart.withPartitionKey(CqlIdentifier.fromInternal(partitionKeyName),
		    tableData.columnMap.get(partitionKeyName));
	});
	Map<CqlIdentifier, ClusteringOrder> clusteringColumnOrderMap = new LinkedHashMap<>();
	tableData.orderedClusterKey.keySet().stream().sorted().forEach(clusterKeyOrder -> {
	    String clusterKeyName = tableData.orderedClusterKey.get(clusterKeyOrder);
	    ClusteringOrder clusteringOrder = ClusteringOrder
		    .valueOf(clusterKeyName.substring(clusterKeyName.indexOf(",") + 1, clusterKeyName.length()));
	    clusterKeyName = clusterKeyName.substring(0, clusterKeyName.indexOf(","));
	    createTable = createTable.withClusteringColumn(CqlIdentifier.fromInternal(clusterKeyName),
		    tableData.columnMap.get(clusterKeyName));
	    clusteringColumnOrderMap.put(CqlIdentifier.fromInternal(clusterKeyName), clusteringOrder);
	});

	tableData.columnMap.forEach((columnName, dataType) -> {
	    if ((!tableData.orderedPartitionKey.containsValue(columnName))
		    && (!tableData.orderedClusterKey.containsValue(columnName))) {
		createTable = createTable.withColumn(CqlIdentifier.fromInternal(columnName), dataType);
	    }
	});

	cassandraSession.execute(
		createTable.withCompaction(tableData.compactionStratergy).withDefaultTimeToLiveSeconds(tableData.ttl)
			.withClusteringOrderByIds(clusteringColumnOrderMap).build());
	LOG.info("{} table in {} keyspace is created", tableName.asInternal(), keySpaceName.asInternal());
	return true;
    }

}
