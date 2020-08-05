module core.framework.web {

	requires transitive core.framework.biz;

	requires transitive spring.security.config;

	requires transitive spring.security.web;

	requires transitive org.aspectj.runtime;

	opens com.jcb.web.config;

	opens com.jcb.web.router to spring.context, spring.beans, spring.core;

	opens com.jcb.web.filter to spring.context, spring.beans, spring.core;

	exports com.jcb.web.config to core.framework;

}