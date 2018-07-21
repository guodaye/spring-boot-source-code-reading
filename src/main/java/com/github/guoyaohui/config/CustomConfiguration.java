package com.github.guoyaohui.config;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * @author 郭垚辉
 * @create_time 2018/7/21
 */
@Configuration
public class CustomConfiguration {

    @PostConstruct
    public void init() {
        System.out.println("init this configuration !");
    }

}
