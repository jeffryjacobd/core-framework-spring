package com.jcb.handlers.spring.initializers;

import com.jcb.config.MainConfig;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.TomcatHttpHandlerAdapter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

public class WebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
	AnnotationConfigWebApplicationContext mainContext = new AnnotationConfigWebApplicationContext();
	mainContext.register(MainConfig.class);
	mainContext.setConfigLocation("com.jcb");
	mainContext.refresh();
	HttpHandler httpHandler = WebHttpHandlerBuilder.applicationContext(mainContext).build();
	ServletRegistration.Dynamic dispatcher = servletContext.addServlet("tomcatHandler",
		new TomcatHttpHandlerAdapter(httpHandler));
	dispatcher.addMapping("/");
	dispatcher.setLoadOnStartup(1);
	dispatcher.setAsyncSupported(true);
    }

}
