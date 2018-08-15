package com.github.guoyaohui.bean;

import javax.annotation.PostConstruct;

/**
 * @author 郭垚辉
 * @date 2018/08/14
 */
public class CustomBean01 implements ICustomBean {

    @PostConstruct
    public void init() {
        System.out.println("CustomBean01： 无他，只是为了查看下这个类何时添加的");
    }

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
