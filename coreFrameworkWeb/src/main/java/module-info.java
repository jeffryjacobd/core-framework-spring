module core.framework.web {

	requires transitive core.framework.biz;

	requires spring.webflux;

	requires spring.security.config;

	requires spring.security.web;

	opens com.jcb.web.config to spring.context, spring.beans, spring.core;

	opens com.jcb.web.router to spring.context, spring.beans, spring.core;

	exports com.jcb.web.config to core.framework;

}