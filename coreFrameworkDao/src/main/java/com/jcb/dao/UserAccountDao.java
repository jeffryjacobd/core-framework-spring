/**
 * 
 */
package com.jcb.dao;

import com.jcb.dto.UserAccountDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Jeffry Jacob D
 *
 */
public interface UserAccountDao {

	public Mono<Boolean> insert(UserAccountDto data);

	public Flux<UserAccountDto> getAll(String... specificColumns);

	public Mono<UserAccountDto> get(String userName);

}
