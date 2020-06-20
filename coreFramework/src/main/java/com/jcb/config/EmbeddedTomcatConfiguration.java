package com.jcb.config;

import static com.jcb.constants.SystemPropertyConstants.TOMCAT_SERVER_PORT;

import java.io.IOException;

import org.springframework.boot.web.embedded.tomcat.TomcatReactiveWebServerFactory;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.boot.web.server.Http2;
import org.springframework.boot.web.server.Ssl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class EmbeddedTomcatConfiguration {

    private static int DEFAULT_PORT = 8081;

    @Bean
    ReactiveWebServerFactory reactiveWebServerFactory() throws IOException {
	Http2 http2 = new Http2();
	http2.setEnabled(true);
	TomcatReactiveWebServerFactory tomcatFactory = new TomcatReactiveWebServerFactory();
	tomcatFactory.setPort(Integer.getInteger(TOMCAT_SERVER_PORT, DEFAULT_PORT));
	tomcatFactory.setHttp2(http2);
	tomcatFactory.setSsl(sslConfig());
	return tomcatFactory;
    }

    private Ssl sslConfig() throws IOException {
	Ssl ssl = new Ssl();
	ssl.setKeyStore(new ClassPathResource("/keystore/com.jcb.p12").getFile().getAbsolutePath());
	ssl.setKeyAlias("jcb");
	ssl.setKeyStorePassword("jeffryjacobd");
	ssl.setKeyStoreType("PKCS12");
	ssl.setEnabledProtocols(new String[] { "TLSv1.3" });
	return ssl;
    }

}
