package com.jcb.handlers.spring.initializer;

import com.jcb.config.MainConfig;
import com.jcb.handlers.logging.initializer.CustomLoggingSystem;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.context.annotation.Import;

import ch.qos.logback.classic.Logger;

@SpringBootApplication
@Import(MainConfig.class)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, ThymeleafAutoConfiguration.class,
	ReactiveWebServerFactoryAutoConfiguration.class })
public class SpringBootApp {

    public static final Logger LOG = (Logger) LoggerFactory.getLogger(SpringBootApp.class);

    public static void main(String[] args) {
	System.setProperty("java.util.logging.SimpleFormatter.format", "");
	System.setProperty("org.springframework.boot.logging.LoggingSystem", CustomLoggingSystem.class.getName());
	LOG.info("Initializing");
	SpringApplication.run(SpringBootApp.class, args);
    }

}
