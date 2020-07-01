package com.jcb.dao.impl;

import com.jcb.constants.enumeration.Gender;
import com.jcb.dao.ExampleDao;
import com.jcb.dto.ExampleDto;

import java.time.LocalDate;

import javax.annotation.PostConstruct;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.qos.logback.classic.Logger;
import reactor.core.publisher.Flux;

@Component
public class ExampleDaoImpl extends AbstractDaoImpl<ExampleDto> implements ExampleDao {

    final static Logger LOG = (Logger) LoggerFactory.getLogger(ExampleDaoImpl.class);

    @PostConstruct
    public void loadData() {
    	Flux.just(1, 2, 3).map(name -> {
		    return ExampleDto.builder().id(name).firstName("Jeffry").middleName("Jacob").lastName("D")
			    .dateOfBirth(LocalDate.now()).gender(Gender.MALE).build();
		}).flatMap(this::insert).blockLast();
    }

}