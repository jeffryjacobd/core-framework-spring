package com.jcb.handlers.spring.initializers;

import com.jcb.config.LoggingConfig;
import com.jcb.config.MainConfig;

import java.io.IOException;

import javax.servlet.ServletException;

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
@Import({ MainConfig.class })
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, ThymeleafAutoConfiguration.class,
	ReactiveWebServerFactoryAutoConfiguration.class })
public class WebAppInitializer {

    public static final Logger LOG = (Logger) LoggerFactory.getLogger(WebAppInitializer.class);

    public static void main(String[] args) throws ServletException, IOException {
	System.setProperty("java.util.logging.SimpleFormatter.format", "");
	System.setProperty("logback.ContextSelector", LoggingConfig.class.getName());
	LOG.info("Initializing");
	// System.setProperty("org.springframework.boot.logging.LoggingSystem", "none");
	SpringApplication.run(WebAppInitializer.class, args);
    }

//    private static void deleteFile(Path filePath) {
//	filePath.toFile().delete();
//    }
//
//    private static Path logbackTweaking() throws IOException {
//	
//	String classPath = System.getProperty("java.class.path").split(File.pathSeparator)[0];
//	Path folderPath = Path.of(classPath, File.separator, "META-INF", File.separator, "services", File.separator);
//	Path filePath = folderPath.resolve(Configurator.class.getName());
//	if (!folderPath.toFile().exists()) {
//	    folderPath.toFile().mkdirs();
//	}
//	filePath.toFile().createNewFile();
//	Files.writeString(filePath, LoggingConfig.class.getName() + "\n ", StandardOpenOption.TRUNCATE_EXISTING,
//		StandardOpenOption.WRITE, StandardOpenOption.SYNC, StandardOpenOption.DSYNC);
//	return filePath;
//    }

}
