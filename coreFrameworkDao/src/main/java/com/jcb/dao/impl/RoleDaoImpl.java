package com.jcb.dao.impl;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.jcb.dao.RoleDao;
import com.jcb.dto.RoleDto;

import ch.qos.logback.classic.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RoleDaoImpl extends AbstractDaoImpl<RoleDto> implements RoleDao {
	Logger LOG = (Logger) LoggerFactory.getLogger(RoleDaoImpl.class);

	@Override
	@CacheEvict("roles")
	public Mono<Boolean> insert(RoleDto data) {
		// TODO check this if working
		LOG.info("Evicting Roles Cache");
		return super.insert(data);
	}

	@Override
	public Flux<RoleDto> getAll() {
		return Flux.fromIterable(getAllBlocking());
	}

	@Cacheable("roles")
	private List<RoleDto> getAllBlocking() {
		return super.getAll().collectList().block();
	}
}
