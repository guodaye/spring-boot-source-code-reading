package com.github.guoyaohui.aspectj;

import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;

/**
 * 不论是否添加@Component,程序都是可以正常执行的
 * 且这个整合到Spring中是一个单例的Bean
 *
 * @author 郭垚辉
 * @date 2018/9/16
 */
public aspect ProfilingAspect {
    /**
     * 切入点：SampleService继承树中所有 public 且以 add 开头的方法。SampleServiceImpl#add(int,int)方法满足这个条件。
     */
    public pointcut methodsToBeProfiled(): execution(public * com.github.guoyaohui.service.TestService.calculateTime());

    Object around(): methodsToBeProfiled(){
        StopWatch sw = new StopWatch(getClass().getSimpleName());
        try {
            sw.start(thisJoinPoint.getSignature().getName());
            return proceed();
        } finally {
            sw.stop();
            System.out.println(sw.prettyPrint());
        }
    }

}
