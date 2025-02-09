module core.framework.dto {

	requires transitive com.fasterxml.jackson.databind;

	requires transitive com.fasterxml.jackson.datatype.jsr310;

	requires transitive lombok;

	requires transitive logback.classic;

	requires transitive logback.core;

	requires transitive org.slf4j;

	requires transitive com.datastax.oss.driver.core;

	exports com.jcb.constants;

	exports com.jcb.constants.enumeration;

	exports com.jcb.annotation;

	exports com.jcb.dto to core.framework.dao, core.framework.biz, core.framework;

	opens com.jcb.dto to core.framework.dao;

}