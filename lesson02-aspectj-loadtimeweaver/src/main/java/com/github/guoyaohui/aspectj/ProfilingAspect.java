package com.github.guoyaohui.aspectj;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.LoadTimeWeavingConfigurer;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * @Aspect是表示定义了一个切面的意思
 *
 * @author 郭垚辉
 * @date 2018/9/16
 */
@Aspect
@Component
@ConditionalOnClass(name = "org.springframework.instrument.InstrumentationSavingAgent")
public class ProfilingAspect implements LoadTimeWeavingConfigurer {

    @Around("methodsToBeProfiled()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        StopWatch sw = new StopWatch(getClass().getSimpleName());
        try {
            sw.start(pjp.getSignature().getName());
            return pjp.proceed();
        } finally {
            sw.stop();
            System.out.println(sw.prettyPrint());
        }
    }

    @Pointcut("execution(public * com.github.guoyaohui.service.TestService.calculateTime())")
    public void methodsToBeProfiled(){}

    /**
     * 不使用-javaagent的指定spring-instrument的方式进行工作
     * @return
     */
    @Override
    public LoadTimeWeaver getLoadTimeWeaver() {
        return new InstrumentationLoadTimeWeaver();
    }
}
