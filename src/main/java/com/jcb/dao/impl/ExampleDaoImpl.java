package com.jcb.dao.impl;

import com.jcb.dao.ExampleDao;
import com.jcb.dto.ExampleDto;
import com.jcb.enumeration.Gender;

import java.time.LocalDate;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class ExampleDaoImpl extends AbstractDaoImpl<ExampleDto> implements ExampleDao {

    @PostConstruct
    public void loadData() {
	redisConnectionFactory.getReactiveConnection().serverCommands().flushAll();
	redisConnectionFactory.getReactiveConnection().serverCommands().getClientName()
		.thenMany(Flux.just(1, 2, 3).map(name -> {
		    return ExampleDto.builder().id(name).firstName("Jeffry").middleName("Jacob").lastName("D ")
			    .dateOfBirth(LocalDate.now()).gender(Gender.MALE).build();
		}).flatMap(this::insert)).thenMany(daoRedisOps.opsForSet().members(ExampleDto.class.getSimpleName()))
		.subscribe(System.out::println);
    }

}