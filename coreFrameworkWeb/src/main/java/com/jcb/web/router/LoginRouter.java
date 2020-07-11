package com.jcb.web.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class LoginRouter {
	@Bean
	public RouterFunction<ServerResponse> sessionRouter() {
		return RouterFunctions.route(RequestPredicates.path("/getSession"),
				request -> ServerResponse.ok().bodyValue(BodyInserters.empty()));
	}

}
