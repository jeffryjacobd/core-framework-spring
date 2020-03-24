package com.jcb.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AspectHandler {

    public void logBefore(JoinPoint joinPoint) {
	System.out.println(joinPoint.getSignature().toString());
    }

}
