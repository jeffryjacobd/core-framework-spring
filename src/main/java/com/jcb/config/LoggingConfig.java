package com.jcb.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.BasicStatusManager;
import ch.qos.logback.core.ConsoleAppender;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LoggingConfig {

    private static LoggerContext configure() {
	LoggerContext loggerContextNew = new LoggerContext();
	loggerContextNew.setStatusManager(new BasicStatusManager());
	ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
	appender.setName("CONSOLE");
	PatternLayoutEncoder encoder = new PatternLayoutEncoder();
	encoder.setContext(loggerContextNew);
	PatternLayout layout = new PatternLayout();
	encoder.setPattern("[%-20thread] %-5level %logger{100} - %msg%n");
	layout.setContext(loggerContextNew);
	encoder.setLayout(layout);
	layout.start();
	encoder.start();
	appender.setContext(loggerContextNew);
	appender.setEncoder(encoder);
	appender.start();
	Logger rootLogger = loggerContextNew.getLogger(Logger.ROOT_LOGGER_NAME);
	rootLogger.addAppender(appender);
	rootLogger.setLevel(Level.ALL);
	loggerContextNew.setName("jeffry");
	loggerContextNew.start();
	return loggerContextNew;
    }

}
