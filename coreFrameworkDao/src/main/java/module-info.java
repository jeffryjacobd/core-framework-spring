module core.framework.dao {

    requires transitive core.framework.dto;

    requires transitive reactor.core;

    requires transitive spring.context;

    requires transitive org.reactivestreams;

    requires transitive spring.beans;

    requires transitive spring.core;

    requires transitive com.datastax.oss.driver.querybuilder;

    requires java.annotation;

    exports com.jcb.dao to core.framework.biz;

    exports com.jcb.handlers.cassandra.listener.schemachange;

    exports com.jcb.handlers.spring.bean.dao;

    opens com.jcb.handlers.cassandra.helper to spring.beans, spring.core, spring.context;

    opens com.jcb.handlers.cassandra.initializer to spring.beans, spring.core, spring.context;

    opens com.jcb.dao.impl to spring.beans, spring.core, spring.context;

}