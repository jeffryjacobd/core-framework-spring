package com.jcb.service.security.impl;

import static com.jcb.entity.WebSession.USER_NAME_KEY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import com.jcb.entity.WebSession;
import com.jcb.service.security.UserAuthenticationService;

import reactor.core.publisher.Mono;

@Component
public class SessionBasedAuthenticationManager implements ReactiveAuthenticationManager {

	@Autowired
	private UserAuthenticationService userAuthenticationProvider;

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		WebSession session = (WebSession) authentication.getPrincipal();
		String userName = session.getAttribute(USER_NAME_KEY).toString();
		if (userName.isBlank()) {
			authentication.setAuthenticated(false);
			return Mono.error(new SessionAuthenticationException("AuthenticatedSession not found"));
		} else {
			return userAuthenticationProvider.findByUsername(userName).map(userDetail -> {
				Authentication auth = new PreAuthenticatedAuthenticationToken(userName, userDetail,
						userDetail.getAuthorities());
				return auth;
			}).switchIfEmpty(Mono.error(new UsernameNotFoundException(userName + "not Found")));
		}
	}

}
