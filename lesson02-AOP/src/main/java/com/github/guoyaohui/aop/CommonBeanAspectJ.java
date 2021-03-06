package com.github.guoyaohui.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/16
 */
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class CommonBeanAspectJ {

    @Pointcut(value = "execution(public * com.github.guoyaohui.bean.CustomBean01.*(..))")
    public void annotaionCheck() {
    }

    @Around(value = "annotaionCheck()")
    public Object calculateTime(ProceedingJoinPoint point) throws Throwable {
        return point.proceed();
    }
}
