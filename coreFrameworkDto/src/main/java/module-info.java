module core.framework.dto {

    requires transitive com.fasterxml.jackson.databind;

    requires transitive com.fasterxml.jackson.datatype.jsr310;

    requires transitive spring.data.redis;

    requires transitive lombok;

    requires transitive java.driver.core;

    requires transitive logback.classic;

    requires transitive logback.core;

    requires transitive org.slf4j;

    exports com.jcb.constants;

    exports com.jcb.constants.enumeration;

    exports com.jcb.annotation;

    exports com.jcb.dto to core.framework.dao;

    opens com.jcb.dto to core.framework.dao;

}