module core.framework.biz {

	requires transitive core.framework.dao;

	requires transitive spring.webflux;

	requires transitive nimbus.jose.jwt;

	exports com.jcb.biz.config to core.framework;

	exports com.jcb.web.handler to core.framework.web;

	opens com.jcb.web.handler to spring.core, spring.beans, spring.context;

	opens com.jcb.web.handler.impl to spring.core, spring.beans, spring.context;

	opens com.jcb.web.handler.config to spring.core, spring.beans, spring.context;

	opens com.jcb.biz.config to spring.core, spring.beans, spring.context;

	opens com.jcb.service.security.config to spring.core, spring.beans, spring.context;

	opens com.jcb.service.security.impl to spring.core, spring.beans, spring.context;

	opens com.jcb.service.crypt.keygeneration.impl to spring.core, spring.beans, spring.context;
}