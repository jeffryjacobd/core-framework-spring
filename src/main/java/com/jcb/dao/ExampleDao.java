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

/**
 * @author Jeffry Jacob D
 *
 */
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ExampleDao {

    private final ReactiveRedisConnectionFactory factory;

    private final ReactiveRedisOperations<String, ExampleDto> exampleDaoRedisOps;

    @PostConstruct
    public void loadData() {
	factory.getReactiveConnection().serverCommands().flushAll().thenMany(Flux.just(1, 2, 3).map(name -> {
	    return ExampleDto.builder().id(name).firstName("Jeffry ").middleName("Jacob ").lastName("D ")
		    .dateOfBirth(LocalDate.now()).gender(Gender.MALE).build();
	}).flatMap(exampleDto -> exampleDaoRedisOps.opsForList().leftPush(exampleDto.getClass().getSimpleName(),
		exampleDto))).thenMany(exampleDaoRedisOps.opsForList().range(ExampleDto.class.getSimpleName(), 0, -1))
		.subscribe(System.out::println);
    }

}
