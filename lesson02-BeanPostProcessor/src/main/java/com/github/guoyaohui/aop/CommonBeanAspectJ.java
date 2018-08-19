package com.github.guoyaohui.aop;

import com.github.guoyaohui.bean.ICustomBean01;
import com.github.guoyaohui.bean.ICustomBean02;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/16
 */
@Aspect
@Component
public class CommonBeanAspectJ {

    @Autowired
    private ICustomBean01 iCustomBean01;
    @Autowired
    private ICustomBean02 iCustomBean02;

    @Pointcut(value = "execution(public * com.github.guoyaohui.bean.CustomBean01.*(..))")
    public void annotaionCheck() {
    }

    @Around(value = "annotaionCheck()")
    public Object calculateTime(ProceedingJoinPoint point) throws Throwable {
        long begin = System.currentTimeMillis();
        Object result = point.proceed();
        System.out.println(" 耗时： " + (System.currentTimeMillis() - begin));
        boolean equals = Objects.equals(iCustomBean01, iCustomBean02);
        return result;
    }
}
