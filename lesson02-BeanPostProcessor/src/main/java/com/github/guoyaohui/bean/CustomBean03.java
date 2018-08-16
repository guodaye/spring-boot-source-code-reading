package com.github.guoyaohui.bean;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/15
 */
@Component
public class CustomBean03 implements ICustomBean {

    private String value;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    public String sayHello() {
        return this.getClass().toString() + " : " + UUID.randomUUID().toString();
    }

}
