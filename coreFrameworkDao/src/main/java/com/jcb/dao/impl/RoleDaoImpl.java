package com.jcb.dao.impl;

import com.jcb.dao.RoleDao;
import com.jcb.dto.RoleDto;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RoleDaoImpl extends AbstractDaoImpl<RoleDto> implements RoleDao {

    Logger LOG = (Logger) LoggerFactory.getLogger(RoleDaoImpl.class);

    private static final int CACHE_LIMIT = 100;

    private static Flux<RoleDto> dbCache = null;

    @Override
    public Mono<Boolean> insert(RoleDto data) {
	// TODO check this if working
	LOG.info("Evicting Roles Cache");
	dbCache = null;
	return super.insert(data);
    }

    @Override
    public Flux<RoleDto> getAll() {
	if (dbCache == null) {
	    dbCache = super.getAll().cache(CACHE_LIMIT);
	}
	return dbCache;
    }

}
