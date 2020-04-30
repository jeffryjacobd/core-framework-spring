package com.jcb.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LoggingConfig extends ContextAwareBase implements Configurator {

    @Override
    public void configure(LoggerContext loggerContext) {
	loggerContext.getName();
    }

}
