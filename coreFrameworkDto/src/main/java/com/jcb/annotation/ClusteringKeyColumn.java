package com.jcb.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface ClusteringKeyColumn {

    public int value();

    public ClusteringOrder clusteringOrder() default ClusteringOrder.ASC;

}
