package com.jcb.handlers.spring.bean.dto;

import com.jcb.annotation.RedisTable;
import com.jcb.utility.UtilityMethodsHelper;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

public class RedisTemplateBeanCreator implements BeanDefinitionRegistryPostProcessor {

    private static List<Class<?>> redisTableClasses = null;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
	try {
	    redisTableClasses = UtilityMethodsHelper.getAnnotatedClassesInPackage("com.jcb.dto", RedisTable.class);
	    redisTableClasses.stream().forEach(redisTableClass -> {
		createSerializerBean(redisTableClass, registry);
		createSerializerContextBean(redisTableClass, registry);
		createRedisOperation(redisTableClass, registry);
	    });
	} catch (ClassNotFoundException | IOException e) {
	    e.printStackTrace();
	} finally {
	    redisTableClasses = null;
	}
    }

    private void createSerializerContextBean(Class<?> redisTableClass, BeanDefinitionRegistry registry) {

	BeanDefinitionBuilder redisSerializationContextbuilder = BeanDefinitionBuilder.genericBeanDefinition()
		.setFactoryMethodOnBean("value", "redisSerializationContextBuilder")
		.addConstructorArgReference(redisTableClass.getSimpleName().replace("Dto", "Dao") + "Serializer");

	registry.registerBeanDefinition(
		redisTableClass.getSimpleName().replace("Dto", "Dao") + "RedisSerializationContextBeanBuilder",
		redisSerializationContextbuilder.getBeanDefinition());

	BeanDefinitionBuilder redisSerializationContext = BeanDefinitionBuilder.genericBeanDefinition()
		.setFactoryMethodOnBean("build",
			redisTableClass.getSimpleName().replace("Dto", "Dao") + "RedisSerializationContextBeanBuilder");

	registry.registerBeanDefinition(
		redisTableClass.getSimpleName().replace("Dto", "Dao") + "RedisSerializationContextBean",
		redisSerializationContext.getBeanDefinition());
    }

    private void createRedisOperation(Class<?> redisTableClass, BeanDefinitionRegistry registry) {

	BeanDefinitionBuilder reactiveRedisTemplateBeanBuilder = BeanDefinitionBuilder
		.genericBeanDefinition(ReactiveRedisTemplate.class)
		.addConstructorArgReference("reactiveRedisConnectionFactory").addConstructorArgReference(
			redisTableClass.getSimpleName().replace("Dto", "Dao") + "RedisSerializationContextBean");

	registry.registerBeanDefinition(redisTableClass.getSimpleName().replace("Dto", "Dao") + "ReactiveRedisTemplate",
		reactiveRedisTemplateBeanBuilder.getBeanDefinition());
    }

    private void createSerializerBean(Class<?> redisTableClass, BeanDefinitionRegistry registry) {
	BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(Jackson2JsonRedisSerializer.class)
		.addConstructorArgValue(redisTableClass);
	registry.registerBeanDefinition(redisTableClass.getSimpleName().replace("Dto", "Dao") + "Serializer",
		builder.getBeanDefinition());
    }

}
