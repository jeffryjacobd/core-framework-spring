module core.framework.biz {

	requires transitive core.framework.dao;

	requires transitive spring.security.core;

	requires transitive nimbus.jose.jwt;

	exports com.jcb.service.crypt.keygeneration to core.framework;

	exports com.jcb.service.crypt.keygeneration.impl to core.framework;

	opens com.jcb.service.crypt.keygeneration to spring.core, spring.beans, spring.context;

	opens com.jcb.service.crypt.keygeneration.impl to spring.core, spring.beans, spring.context;
}