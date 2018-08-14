package com.github.guoyaohui.bean;

import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/14
 */
@Component
public class CustomBean01 implements ICustomBean {

    private String value;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
