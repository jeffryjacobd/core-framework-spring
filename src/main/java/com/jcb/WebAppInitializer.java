package com.jcb;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.jcb.config.MainConfig;

public class WebAppInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		AnnotationConfigWebApplicationContext mainContext = new AnnotationConfigWebApplicationContext();
		mainContext.setConfigLocation(MainConfig.class.getName());
		ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher",
				new DispatcherServlet(mainContext));
		dispatcher.addMapping("/");
		dispatcher.setLoadOnStartup(1);
	}

}
