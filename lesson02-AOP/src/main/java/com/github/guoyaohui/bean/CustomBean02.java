package com.github.guoyaohui.bean;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/15
 */
// implements ICustomBean02
@Component
public class CustomBean02 implements ICustomBean02 {

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
