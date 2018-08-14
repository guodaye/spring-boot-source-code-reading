package com.github.guoyaohui;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author 郭垚辉
 * @create_time 2018/7/21
 */
@SpringBootApplication
public class Lesson01Application {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Lesson01Application.class);
        application.setBannerMode(Mode.OFF);
        ConfigurableApplicationContext context = application.run(args);
        // 发布context的start事件
        context.start();

        // 发布context的stop事件
        context.stop();
    }

}
