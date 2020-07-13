/**
 * 
 */
package com.jcb.dao;

import com.jcb.dto.RoleDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author jeffry
 *
 */
public interface RoleDao {
	public Mono<Boolean> insert(RoleDto data);

	public Flux<RoleDto> getAll();

	public Mono<RoleDto> get(String role);
}
