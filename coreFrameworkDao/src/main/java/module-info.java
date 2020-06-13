module core.framework.dao {

    requires transitive tomcat.annotations.api;

    requires transitive core.framework.dto;

    requires transitive reactor.core;

    requires transitive spring.context;

    requires transitive org.reactivestreams;

    requires transitive spring.beans;

    requires transitive spring.core;

    requires transitive spring.boot;
    
    requires transitive com.datastax.oss.driver.querybuilder;

    exports com.jcb.dao to core.framework.biz;

    exports com.jcb.handlers.cassandra.listener.schemachange;

    exports com.jcb.handlers.spring.bean.dao;

    exports com.jcb.handlers.spring.bean.dto;

    opens com.jcb.handlers.cassandra.helper to spring.beans, spring.core, spring.context;

    opens com.jcb.handlers.cassandra.initializer to spring.beans, spring.core, spring.context;

    opens com.jcb.dao.impl to spring.beans, spring.core, spring.context;

}