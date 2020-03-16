package com.jcb.handlers.spring.bean.dao;

import com.jcb.annotation.RedisTable;
import com.jcb.handlers.spring.bean.AbstractTemplateBeanCreator;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowire;
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
		.genericBeanDefinition(cassandraTableDaoClass);
	Class<?> cassandraDtoClass = null;
	try {
	    String parentClassWithGenerics = cassandraTableDaoClass.getGenericSuperclass().getTypeName();
	    cassandraDtoClass = getClassWithFullName(parentClassWithGenerics
		    .substring(parentClassWithGenerics.indexOf("<") + 1, parentClassWithGenerics.indexOf(">")));
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}
	if (cassandraDtoClass.getAnnotation(RedisTable.class) == null) {
	    daoImplContextbuilder = daoImplContextbuilder.addConstructorArgValue(null).addConstructorArgValue(null);
	} else {
	    daoImplContextbuilder = daoImplContextbuilder.addConstructorArgReference("reactiveRedisConnectionFactory")
		    .addConstructorArgReference(
			    cassandraTableDaoClass.getSimpleName().replace("DaoImpl", "Dao") + "ReactiveRedisTemplate");
	}
	daoImplContextbuilder = daoImplContextbuilder.addConstructorArgReference("cassandraSession")
		.addConstructorArgReference("boundStatementMap").addConstructorArgReference("batchStatementBuilder")
		.setAutowireMode(Autowire.BY_TYPE.value());

	registry.registerBeanDefinition(cassandraTableDaoClass.getSimpleName().replace("DaoImpl", "Dao"),
		daoImplContextbuilder.getBeanDefinition());
    }

}
