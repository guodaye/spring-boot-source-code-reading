package com.github.guoyaohui;

import com.github.guoyaohui.core.TestComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author 郭垚辉
 * @create_time 2018/7/21
 */
@SpringBootApplication
public class ReadSpringServer {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(ReadSpringServer.class, args);
        TestComponent bean = run.getBean(TestComponent.class);
    }

}
