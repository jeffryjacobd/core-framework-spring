/**
 * 
 */
package com.jcb.dao;

import com.jcb.dto.ExampleDto;

import reactor.core.publisher.Mono;

/**
 * @author Jeffry Jacob D
 *
 */
public interface ExampleDao {

    public Mono<Boolean> insert(ExampleDto data);

}
