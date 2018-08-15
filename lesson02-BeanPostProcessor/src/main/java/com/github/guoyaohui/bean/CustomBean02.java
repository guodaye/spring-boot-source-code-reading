package com.github.guoyaohui.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/15
 */
@Component
public class CustomBean02 {

    @Autowired
    private CustomBean03 customBean03;

    public String sayHello() {
        return this.getClass().toString() + " : " + customBean03.sayHello();
    }

}
