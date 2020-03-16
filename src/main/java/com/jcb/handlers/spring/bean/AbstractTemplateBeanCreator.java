package com.jcb.handlers.spring.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

@Configuration
public abstract class AbstractTemplateBeanCreator implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public abstract void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

    @SuppressWarnings("unchecked")
    private boolean isCandidate(MetadataReader metadataReader, @SuppressWarnings("rawtypes") Class annotationClass)
	    throws ClassNotFoundException {
	Class<?> c = Class.forName(metadataReader.getClassMetadata().getClassName());
	if (c.getAnnotation(annotationClass) != null) {
	    return true;
	}
	return false;
    }

    private String resolveBasePackage(String basePackage) {
	return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    protected List<Class<?>> getAnnotatedClassesInPackage(String packagePath, Class<?> annotatation)
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
		    candidates.add(getClassWithFullName(metadataReader.getClassMetadata().getClassName()));
		}
	    }
	}
	return candidates;
    }

    protected Class<?> getClassWithFullName(String classNameWithPackage) throws ClassNotFoundException {
	return Class.forName(classNameWithPackage);

    }

}
