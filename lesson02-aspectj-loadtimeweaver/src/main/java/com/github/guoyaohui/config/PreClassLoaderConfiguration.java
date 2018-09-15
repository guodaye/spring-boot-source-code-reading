package com.github.guoyaohui.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

/**
 * @author 郭垚辉
 * @date 2018/9/16
 */
@EnableAspectJAutoProxy
@Configuration
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.AUTODETECT)
public class PreClassLoaderConfiguration {

}
