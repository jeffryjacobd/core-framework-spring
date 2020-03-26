package com.jcb.handlers.spring.bean.dao;

import com.jcb.annotation.RedisTable;
import com.jcb.utility.UtilityMethodsHelper;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class DaoTemplateBeanCreator implements BeanDefinitionRegistryPostProcessor {

    private static List<Class<?>> cassandraTableDaoClasses = null;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
	try {
	    cassandraTableDaoClasses = UtilityMethodsHelper.getAnnotatedClassesInPackage("com.jcb.dao.impl",
		    Component.class);
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
	    cassandraDtoClass = UtilityMethodsHelper.getClassWithFullName(parentClassWithGenerics
		    .substring(parentClassWithGenerics.indexOf("<") + 1, parentClassWithGenerics.indexOf(">")));
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	    throw new Error(e);
	}
	if (cassandraDtoClass.getAnnotation(RedisTable.class) == null) {
	    daoImplContextbuilder = daoImplContextbuilder.addConstructorArgValue(null).addConstructorArgValue(null);
	} else {
	    daoImplContextbuilder = daoImplContextbuilder
		    .addPropertyReference("redisConnectionFactory", "reactiveRedisConnectionFactory")
		    .addPropertyReference("daoRedisOps",
			    cassandraTableDaoClass.getSimpleName().replace("DaoImpl", "Dao") + "ReactiveRedisTemplate");
	}
	createTableMetaDataBean(registry, cassandraDtoClass);
	daoImplContextbuilder = daoImplContextbuilder.addPropertyReference("cassandraSession", "cassandraSession")
		.addPropertyReference("boundStatementMap", "boundStatementMap")
		.addPropertyReference("batchStatementbuilder", "batchStatementBuilder")
		.addPropertyValue("dtoClass", cassandraDtoClass)
		.addPropertyReference("tableMetaData",
			cassandraDtoClass.getSimpleName().replace("Dto", "Dao") + "TableMetaData")
		.setAutowireMode(Autowire.BY_TYPE.value());

	registry.registerBeanDefinition(cassandraTableDaoClass.getSimpleName().replace("DaoImpl", "Dao"),
		daoImplContextbuilder.getBeanDefinition());
    }

    private void createTableMetaDataBean(BeanDefinitionRegistry registry, Class<?> cassandraDtoClass) {
	BeanDefinitionBuilder tableMetaDatabuilder = BeanDefinitionBuilder.genericBeanDefinition()
		.setFactoryMethodOnBean("initializeTableMetaData", "cassandraDbInitializerHelper")
		.addConstructorArgValue(cassandraDtoClass);
	registry.registerBeanDefinition(cassandraDtoClass.getSimpleName().replace("Dto", "Dao") + "TableMetaData",
		tableMetaDatabuilder.getBeanDefinition());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

}
