package com.jcb.config;

import static com.jcb.constants.SystemPropertyConstants.TOMCAT_SERVER_PORT;

import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.boot.web.server.Http2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedTomcatConfiguration {

    private static int DEFAULT_PORT = 8081;

    @Bean
    ReactiveWebServerFactory reactiveWebServerFactory() {
	Http2 http2 = new Http2();
	http2.setEnabled(true);
	TomcatReactiveWebServerFactory tomcatFactory = new TomcatReactiveWebServerFactory();
	tomcatFactory.setPort(Integer.getInteger(TOMCAT_SERVER_PORT, DEFAULT_PORT));
	tomcatFactory.setHttp2(http2);
	return tomcatFactory;
    }

}
