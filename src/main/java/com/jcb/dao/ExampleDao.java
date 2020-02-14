/**
 * 
 */
package com.jcb.dao;

import com.jcb.dto.ExampleDto;

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

    private final ReactiveRedisOperations<String, ExampleDto> exampleDaoOps;

    @PostConstruct
    public void loadData() {
	factory.getReactiveConnection().serverCommands().flushAll()
		.thenMany(Flux.just("Jet Black Redis", "Darth Redis", "Black Alert Redis").map(name -> {
		    return ExampleDto.builder().id(name).name("Jeffry " + name).dateOfBirth(LocalDate.now()).build();
		}).flatMap(coffee -> exampleDaoOps.opsForValue().set(coffee.getId(), coffee)))
		.thenMany(exampleDaoOps.keys("*").flatMap(exampleDaoOps.opsForValue()::get))
		.subscribe(System.out::println);
    }

}
