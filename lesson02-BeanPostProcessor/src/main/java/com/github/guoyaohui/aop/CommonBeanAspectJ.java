package com.github.guoyaohui.aop;

import com.github.guoyaohui.bean.CustomBean;
import com.github.guoyaohui.bean.ICustomBean;
import java.util.Objects;
import javax.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/16
 */
@Aspect
@Component
public class CommonBeanAspectJ {

    @Autowired
    private ICustomBean iCustomBean;

    @Pointcut(value = "execution(public * com.github.guoyaohui.bean.CustomBean.*(..))")
    public void annotaionCheck() {
    }

    @Around(value = "annotaionCheck()")
    public Object calculateTime(ProceedingJoinPoint point) throws Throwable {
        long begin = System.currentTimeMillis();
        Object result = point.proceed();
        System.out.println(" 耗时： " + (System.currentTimeMillis() - begin));
        boolean equals = Objects.equals(iCustomBean, null);
        return result;
    }
}
