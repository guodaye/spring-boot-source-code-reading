package com.github.guoyaohui.bean.factory;

import javax.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @author 郭垚辉
 * @date 2018/08/15
 */
//@Component
public class CustomFactoryBeanPostProcessor implements BeanFactoryPostProcessor {

    @PostConstruct
    public void init() {
        System.out.println("CustomFactoryBeanPostProcessor： 无他，只是为了查看下这个类何时添加的");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("打酱油的CustomFactoryBeanPostProcessor");
    }
}
