package com.github.guoyaohui.config;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @create_time 2018/7/21
 */
//@Configuration
@Component
public class CustomConfiguration2 {

    @Bean
    public Object resutl() {
        Object o = new Object();
        return o;
    }

    @PostConstruct
    public void init() {
        System.out.println(resutl());
        System.out.println("init this configuration !");
    }

}
