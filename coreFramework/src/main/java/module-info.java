/**
 * 
 */
/**
 * @author Jeffry Jacob D
 *
 */
module core.framework {

	requires java.base;

	requires core.framework.web;

	requires spring.boot.autoconfigure;

	requires spring.webflux;

	requires org.apache.commons.lang3;

	requires spring.boot;

	opens com.jcb.handlers.spring.initializer to spring.core, spring.beans, spring.context;

	opens com.jcb.handlers.logging.initializer to spring.core, spring.beans, spring.context, spring.boot;

	opens com.jcb.handlers.jwt to spring.core, spring.beans, spring.context;

	opens com.jcb.handlers.spring.session.initializer;

	opens com.jcb.config to spring.core, spring.beans, spring.context;

	opens keystore;

}