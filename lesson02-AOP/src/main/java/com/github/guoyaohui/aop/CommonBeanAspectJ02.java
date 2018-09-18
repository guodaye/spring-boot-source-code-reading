package com.github.guoyaohui.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/16
 */
@Aspect
@Component
public class CommonBeanAspectJ02 {

    @Pointcut(value = "execution(public * com.github.guoyaohui.bean.CustomBean01.*(..))")
    public void annotaionCheck2() {
    }

    @Around(value = "annotaionCheck2()")
    public Object calculateTime(ProceedingJoinPoint point) throws Throwable {
        return point.proceed();
    }
}
