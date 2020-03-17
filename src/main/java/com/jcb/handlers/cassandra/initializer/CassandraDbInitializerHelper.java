package com.jcb.handlers.cassandra.initializer;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.jcb.annotation.CassandraTable;
import com.jcb.constants.SystemPropertyConstants;
import com.jcb.utility.UtilityMethodsHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import reactor.core.publisher.Mono;

public class CassandraDbInitializerHelper {

    public static Boolean keySpaceInitialized = false;

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

    public static Boolean initializeTable(CqlSession cassandraSession) {
	return null;

    }

}
