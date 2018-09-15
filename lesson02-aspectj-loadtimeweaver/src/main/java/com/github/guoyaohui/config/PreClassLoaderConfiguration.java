package com.github.guoyaohui.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

/**
 * @author 郭垚辉
 * @date 2018/9/16
 */

@Configuration
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.AUTODETECT)
@ConditionalOnClass(name = "org.springframework.instrument.InstrumentationSavingAgent")
public class PreClassLoaderConfiguration {

}
