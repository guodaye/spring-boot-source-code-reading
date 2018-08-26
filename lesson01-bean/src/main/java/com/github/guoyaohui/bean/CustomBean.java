package com.github.guoyaohui.bean;

import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/15
 */

@Component
public class CustomBean {

    private String value;

    @PostConstruct
    public void init() {
        System.out.println(this.getClass().getSimpleName());
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String sayHello() {
        return this.getClass().toString() + " : " + UUID.randomUUID().toString();
    }
}
