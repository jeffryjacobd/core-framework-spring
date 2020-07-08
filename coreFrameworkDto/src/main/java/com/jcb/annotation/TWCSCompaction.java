package com.jcb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TWCSCompaction {

    boolean enabled() default true;

    int tombStoneCompactionInterval() default 864000;

    double tombstoneThreshold() default 0.2;

    boolean uncheckedTombStoneCompaction() default false;

    TimeWindowUnit compactionWindowUnit() default TimeWindowUnit.DAYS;

    TimestampResolution timestampResolution() default TimestampResolution.MILLISECONDS;

    long compactionWindowSize() default 1;

    public enum TimeWindowUnit {
	MINUTES, HOURS, DAYS
    }

    public enum TimestampResolution {
	MICROSECONDS, MILLISECONDS
    }

}
