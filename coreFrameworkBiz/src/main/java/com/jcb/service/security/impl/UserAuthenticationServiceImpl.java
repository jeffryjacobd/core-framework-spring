/**
 * 
 */
package com.jcb.service.security.impl;

import org.springframework.security.core.userdetails.UserDetails;

import com.jcb.service.security.UserAuthenticationService;

import reactor.core.publisher.Mono;

/**
 * @author jeffry
 *
 */
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

	@Override
	public Mono<UserDetails> findByUsername(String username) {
		// TODO Auto-generated method stub
		return null;
	}

}
