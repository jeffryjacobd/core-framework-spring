module core.framework.dao {

	requires transitive core.framework.dto;

	requires transitive reactor.core;

	requires transitive spring.context;

	requires transitive org.reactivestreams;

	requires transitive spring.beans;

	requires transitive spring.core;

	requires transitive spring.security.core;

	requires transitive com.datastax.oss.driver.querybuilder;

	requires transitive spring.session.core;

	requires transitive spring.web;

	requires transitive java.annotation;

	exports com.jcb.dao to core.framework.biz, core.framework;

	exports com.jcb.handlers.cassandra.listener.schemachange;

	exports com.jcb.handlers.spring.bean.dao;

	exports com.jcb.entity to core.framework, core.framework.biz;

	opens com.jcb.handlers.cassandra.helper to spring.beans, spring.core, spring.context;

	opens com.jcb.handlers.cassandra.initializer to spring.beans, spring.core, spring.context;

	opens com.jcb.dao.impl to spring.beans, spring.core, spring.context;

	opens com.jcb.dao to spring.beans, spring.core, spring.context, core.framework;

	opens com.jcb.entity to com.fasterxml.jackson.databind;

}