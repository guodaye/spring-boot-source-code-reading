package com.github.guoyaohui.bean;

import javax.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/14
 */
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {

    @PostConstruct
    public void init() {
        System.out.println("CustomBeanPostProcessor： 无他，只是为了查看下这个类何时添加的");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ICustomBean02) {
            String value = ((ICustomBean02) bean).getValue();
            if (value == null || value.length() == 0) {
                ((ICustomBean02) bean).setValue("defaultValue");
            }
        }
        return bean;
    }
}
