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
public class CommonBeanAspectJ {

    @Pointcut(value = "execution(public * com.github.guoyaohui.bean.CustomBean.*(..))")
    public void annotaionCheck() {
    }

    @Around(value = "annotaionCheck()")
    public Object calculateTime(ProceedingJoinPoint point) throws Throwable {
        long begin = System.currentTimeMillis();
        Object result = point.proceed();
        System.out.println(" 耗时： " + (System.currentTimeMillis() - begin));
        return result;
    }
}
