package com.jcb.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "com.jcb.handlers.cassandra.helper", "com.jcb.handlers.cassandra.initializer" })
public class CassandraUtilityConfig {

}
