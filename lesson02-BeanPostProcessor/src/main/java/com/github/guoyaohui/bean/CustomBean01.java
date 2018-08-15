package com.github.guoyaohui.bean;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/14
 */
@Component
@ConfigurationProperties("file")
public class CustomBean01 implements ICustomBean {

    private String name;
    private int age;
    @Autowired
    private CustomBean02 customBean02;
    @Autowired
    private CustomBean03 customBean03;
    @Autowired
    private ICustomBean iCustomBean;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @PostConstruct
    public void init() {
        System.out.println("CustomBean01： 无他，只是为了查看下这个类何时添加的");
    }

    private String value;

    @Override
    public String getValue() {
        String s = customBean02.sayHello();
        String s1 = customBean03.sayHello();
        String value = iCustomBean.toString();
        System.out.println(value);
        System.out.println(s1);
        return s + "  :  " + this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
