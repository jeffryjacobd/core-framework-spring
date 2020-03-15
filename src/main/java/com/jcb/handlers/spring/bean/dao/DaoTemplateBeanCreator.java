package com.jcb.handlers.spring.bean.dao;

import com.jcb.handlers.spring.bean.AbstractTemplateBeanCreator;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class DaoTemplateBeanCreator extends AbstractTemplateBeanCreator {

    private static List<Class<?>> cassandraTableDaoClasses = null;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
	try {
	    cassandraTableDaoClasses = getAnnotatedClassesInPackage("com.jcb.dao.impl", Component.class);
	    cassandraTableDaoClasses.stream().forEach(cassandraTableDaoClass -> {
		createDaoBean(registry, cassandraTableDaoClass);
	    });
	} catch (ClassNotFoundException | IOException e) {
	    e.printStackTrace();
	} finally {
	    cassandraTableDaoClasses = null;
	}

    }

    private void createDaoBean(BeanDefinitionRegistry registry, Class<?> cassandraTableDaoClass) {
	BeanDefinitionBuilder daoImplContextbuilder = BeanDefinitionBuilder
		.genericBeanDefinition(cassandraTableDaoClass)
		.addConstructorArgReference("reactiveRedisConnectionFactory")
		.addConstructorArgReference(
			cassandraTableDaoClass.getSimpleName().replace("DaoImpl", "Dao") + "ReactiveRedisTemplate")
		.addConstructorArgReference("cassandraSession").addConstructorArgReference("preparedStatementMap");

	registry.registerBeanDefinition(cassandraTableDaoClass.getSimpleName().replace("DaoImpl", "Dao"),
		daoImplContextbuilder.getBeanDefinition());
    }

}
