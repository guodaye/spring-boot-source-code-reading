package com.github.guoyaohui.config;

import java.lang.instrument.ClassFileTransformer;
import javax.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.EnableLoadTimeWeaving.AspectJWeaving;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;

/**
 * @author 郭垚辉
 * @date 2018/9/16
 */

@Configuration
@EnableLoadTimeWeaving(aspectjWeaving = AspectJWeaving.ENABLED)
@ConditionalOnClass(name = "org.springframework.instrument.InstrumentationSavingAgent")
public class PreClassLoaderConfiguration extends InstrumentationLoadTimeWeaver {

    @PostConstruct
    public void res3t() {
        System.out.println();
    }
//    /**
//     * 不使用-javaagent的指定spring-instrument的方式进行工作
//     * @return
//     */
//    @Override
//    public LoadTimeWeaver getLoadTimeWeaver() {
//        return new InstrumentationLoadTimeWeaver();
//    }


    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        super.addTransformer(transformer);
    }
}
