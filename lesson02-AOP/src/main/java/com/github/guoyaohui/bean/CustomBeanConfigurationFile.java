package com.github.guoyaohui.bean;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;

/**
 * @author 郭垚辉
 * @date 2018/08/26
 */
@Role(value = BeanDefinition.ROLE_SUPPORT)
public class CustomBeanConfigurationFile {

    @PostConstruct
    public void init() {
        System.out.println("66666");
    }
}
