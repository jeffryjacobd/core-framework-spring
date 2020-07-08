package com.jcb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface STCSCompaction {

    boolean enabled() default true;

    int tombStoneCompactionInterval() default 864000;

    double tombstoneThreshold() default 0.2;

    boolean uncheckedTombStoneCompaction() default false;

    double bucketHigh() default 1.5;

    double bucketLow() default 0.5;

    int maxThreshold() default 32;

    long minSSTableSizeInBytes() default 52428800;

    int minThreshold() default 4;

    boolean purgeRepairedTombstones() default false;

}
