package com.jcb.handlers.spring.bean.redis;

import com.jcb.annotation.RedisTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

@Configuration
public class RedisTemplateBeanCreator implements BeanDefinitionRegistryPostProcessor {

    private static List<Class<?>> redisTableClasses = null;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
	try {
	    redisTableClasses = getAnnotatedClassesInPackage("com.jcb.dto", RedisTable.class);
	    redisTableClasses.stream().forEach(redisTableClass -> {
		createSerializerBean(redisTableClass, registry);
		createSerializerContextBean(redisTableClass, registry);
		createRedisOperation(redisTableClass, registry);
	    });
	} catch (ClassNotFoundException | IOException e) {
	    e.printStackTrace();
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

    @SuppressWarnings("unchecked")
    private boolean isCandidate(MetadataReader metadataReader, Class annotationClass) throws ClassNotFoundException {
	Class<?> c = Class.forName(metadataReader.getClassMetadata().getClassName());
	if (c.getAnnotation(annotationClass) != null) {
	    return true;
	}
	return false;
    }

    private String resolveBasePackage(String basePackage) {
	return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    private List<Class<?>> getAnnotatedClassesInPackage(String packagePath, Class<?> annotatation)
	    throws IOException, ClassNotFoundException {
	ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
	MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

	List<Class<?>> candidates = new ArrayList<Class<?>>();
	String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resolveBasePackage(packagePath)
		+ "/" + "**/*.class";
	Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
	for (Resource resource : resources) {
	    if (resource.isReadable()) {
		MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
		if (isCandidate(metadataReader, annotatation)) {
		    candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
		}
	    }
	}
	return candidates;
    }

}
