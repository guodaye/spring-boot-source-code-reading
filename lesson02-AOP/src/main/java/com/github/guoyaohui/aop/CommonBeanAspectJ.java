package com.github.guoyaohui.aop;

import com.github.guoyaohui.bean.CustomBeanConfigurationFile;
import com.github.guoyaohui.bean.ICustomBean01;
import javax.annotation.PostConstruct;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private CustomBeanConfigurationFile customBeanConfigurationFile;

    @Value("name")
    private String name;

    @PostConstruct
    public void init() {
        System.out.println("hello");
    }

    @Pointcut(value = "execution(public * com.github.guoyaohui.bean.CustomBean01.*(..))")
    public void annotaionCheck() {
    }

    @Around(value = "annotaionCheck()")
    public Object calculateTime(ProceedingJoinPoint point) throws Throwable {
        return point.proceed();
    }
}
