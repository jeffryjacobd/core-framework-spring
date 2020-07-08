package com.jcb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LCSCompaction {

    boolean enabled() default true;

    int tombStoneCompactionInterval() default 864000;

    double tombstoneThreshold() default 0.2;

    boolean uncheckedTombStoneCompaction() default false;

    int ssTableSizeInMB() default 160;

}
