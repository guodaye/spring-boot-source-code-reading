package com.github.guoyaohui.core;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/06
 */
@Component
public class CustomTestBean implements InitializingBean {

    @PostConstruct
    public void init() {
        System.out.println("PostConstruct");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("InitializingBean");
    }

}
