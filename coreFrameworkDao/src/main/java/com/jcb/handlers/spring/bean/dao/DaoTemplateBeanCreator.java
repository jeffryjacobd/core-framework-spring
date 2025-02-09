package com.jcb.handlers.spring.bean.dao;

import java.io.IOException;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

import com.jcb.utility.UtilityMethodsHelper;

import ch.qos.logback.classic.Logger;

public class DaoTemplateBeanCreator implements BeanDefinitionRegistryPostProcessor {
	final static Logger LOG = (Logger) LoggerFactory.getLogger(DaoTemplateBeanCreator.class);

	private static List<Class<?>> cassandraTableDaoClasses = null;

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		try {
			cassandraTableDaoClasses = UtilityMethodsHelper.getAnnotatedClassesInPackage("com.jcb.dao.impl",
					Component.class);
			cassandraTableDaoClasses.stream().forEach(cassandraTableDaoClass -> {
				createDaoBean(registry, cassandraTableDaoClass);
				LOG.info("Generated cassandra related beans for class {}", cassandraTableDaoClass.getName());
			});
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
