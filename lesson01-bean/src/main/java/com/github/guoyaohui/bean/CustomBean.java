package com.github.guoyaohui.bean;

import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/15
 */

@Component
//@Scope不会执行@PreDestroy的方法
//@Scope(value = "prototype", proxyMode = ScopedProxyMode.NO) -- 采用getBean不会有问题
//@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS) --若要使用@Autowired则必须使用代理模式
public class CustomBean {

    private String value;

    @PostConstruct
    public void init() {
        System.out.println("init..." + this.getClass().getSimpleName());
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

    @PreDestroy
    public void destory() {
        System.out.println("destory..." + this.getClass().getSimpleName());
    }
}
