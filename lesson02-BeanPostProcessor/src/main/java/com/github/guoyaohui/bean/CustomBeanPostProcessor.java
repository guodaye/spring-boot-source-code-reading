package com.github.guoyaohui.bean;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/14
 */
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ICustomBean) {
            String value = ((ICustomBean) bean).getValue();
            if (value == null || value.length() == 0) {
                ((ICustomBean) bean).setValue("defaultValue");
            }
        }
        return bean;
    }
}
