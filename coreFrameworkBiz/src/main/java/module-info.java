module core.framework.biz {

	requires transitive core.framework.dao;

	requires transitive spring.webflux;

	requires transitive spring.security.web;

	requires transitive nimbus.jose.jwt;

	requires org.bouncycastle.provider;

	requires transitive org.apache.commons.lang3;

	exports com.jcb.biz.config to core.framework;

	exports com.jcb.web.handler to core.framework.web;

	exports com.jcb.handlers.spring.session.config to core.framework;

	exports com.jcb.service.crypt.keygeneration to core.framework.web, core.framework;

	exports com.jcb.service.security to core.framework.web, core.framework;

	opens com.jcb.handlers.spring.session.config to spring.core, spring.beans, spring.context;

	opens com.jcb.web.handler to spring.core, spring.beans, spring.context;

	opens com.jcb.web.handler.impl to spring.core, spring.beans, spring.context;

	opens com.jcb.web.handler.config to spring.core, spring.beans, spring.context;

	opens com.jcb.biz.config to spring.core, spring.beans, spring.context;

	opens com.jcb.handlers.spring.session.initializer to spring.core, spring.beans, spring.context;

	opens com.jcb.service.security.config to spring.core, spring.beans, spring.context;

	opens com.jcb.service.security.impl to spring.core, spring.beans, spring.context;

	opens com.jcb.service.crypt.keygeneration.impl to spring.core, spring.beans, spring.context;
}