package com.jcb.web.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.jcb.web.handler.LoginHandler;

@Configuration
public class LoginRouter {
	@Bean
	public RouterFunction<ServerResponse> loginRouter(LoginHandler handler) {
		return RouterFunctions.route(RequestPredicates.POST("/getSession"), handler::getSession)
				.andRoute(RequestPredicates.POST("/login"), handler::login);
	}

}
