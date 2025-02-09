package com.jcb.service.security;

import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;

public interface UserAuthenticationService extends ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

}
