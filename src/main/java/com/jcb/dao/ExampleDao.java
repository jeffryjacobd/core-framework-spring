/**
 * 
 */
package com.jcb.dao;

import com.jcb.dto.ExampleDto;

import reactor.core.publisher.Flux;

/**
 * @author Jeffry Jacob D
 *
 */
public interface ExampleDao {

    public Flux<Long> insert(ExampleDto data);

}
