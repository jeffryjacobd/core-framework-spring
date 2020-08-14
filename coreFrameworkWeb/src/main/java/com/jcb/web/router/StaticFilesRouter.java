package com.jcb.web.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class StaticFilesRouter {

	@Bean
	public RouterFunction<ServerResponse> ngRouter() {
		return RouterFunctions
				.route(RequestPredicates.path(""),
						request -> ServerResponse.ok()
								.bodyValue(new ClassPathResource("/static/core-framework-ui/index.html")))
				.and(RouterFunctions.resources("/core-framework-ui/**",
						new ClassPathResource("/static/core-framework-ui/")));
	}

}
