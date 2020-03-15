package com.jcb.config;

import static com.jcb.constants.SystemPropertyConstants.CASSANDRA_POINTS_PROPERTY;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CassandraConfig {

    @Bean("cassandraSession")
    CompletionStage<CqlSession> cassandraSession() {
	CompletionStage<CqlSession> cqlSession = CqlSession.builder().addContactPoints(getContactPoints())
		.withLocalDatacenter("DC1").buildAsync();
	return cqlSession;
    }

    private static Collection<InetSocketAddress> getContactPoints() {
	Collection<InetSocketAddress> addressList = new ArrayList<InetSocketAddress>();
	String cassandraPoints = System.getProperty(CASSANDRA_POINTS_PROPERTY);
	if (StringUtils.isEmpty(cassandraPoints)) {
	    addressList.add(InetSocketAddress.createUnresolved("127.0.0.1", 9042));
	} else {
	    for (String cassandraPoint : cassandraPoints.split(",")) {
		addressList.add(InetSocketAddress.createUnresolved(
			cassandraPoint.substring(0, cassandraPoint.indexOf(":")).trim(), Integer.valueOf(cassandraPoint
				.substring(cassandraPoint.indexOf(":") + 1, cassandraPoint.length()).trim())));
	    }
	}
	return addressList;
    }

    @Bean("preparedStatementMap")
    Map<String, PreparedStatement> getPreparedStatementMap() {
	return new HashMap<String, PreparedStatement>();

    }

}
