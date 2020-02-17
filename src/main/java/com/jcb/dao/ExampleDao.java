/**
 * 
 */
package com.jcb.dao;

import com.jcb.dto.ExampleDto;
import com.jcb.enumeration.Gender;

import java.time.LocalDate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Jeffry Jacob D
 *
 */
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ExampleDao {

    private final ReactiveRedisConnectionFactory redisConnectionFactory;

    private final ReactiveRedisOperations<String, ExampleDto> exampleDaoRedisOps;

    @PostConstruct
    public void loadData() {
	redisConnectionFactory.getReactiveConnection().serverCommands().getClientName()
		.thenMany(Flux.just(1, 2, 3).map(name -> {
		    return ExampleDto.builder().id(name).firstName("Jeffry  ").middleName("Jacob ").lastName("D ")
			    .dateOfBirth(LocalDate.now()).gender(Gender.MALE).build();
		}).flatMap(this::insert))
		.thenMany(exampleDaoRedisOps.opsForSet().members(ExampleDto.class.getSimpleName()))
		.subscribe(System.out::println);
    }

    public Mono<Long> insert(ExampleDto data) {
	return exampleDaoRedisOps.opsForSet().add(ExampleDto.class.getSimpleName(), data);
    }

}
