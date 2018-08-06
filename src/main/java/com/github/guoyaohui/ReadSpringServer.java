package com.github.guoyaohui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author 郭垚辉
 * @create_time 2018/7/21
 */
@SpringBootApplication
public class ReadSpringServer {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(ReadSpringServer.class, args);
        context.start(); // 触发ContextStartedEvent事件
        Thread.sleep(1000);
        context.stop(); // 触发ContextStoppedEvent
    }

}
