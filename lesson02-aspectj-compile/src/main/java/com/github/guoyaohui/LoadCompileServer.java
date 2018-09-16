package com.github.guoyaohui;

import com.github.guoyaohui.service.TestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * AspectJ的类加载期织入
 *
 * @author 郭垚辉
 * @date 2018/9/16
 */
@SpringBootApplication
public class LoadCompileServer {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(LoadCompileServer.class, args);
        TestService bean0 = context.getBean(TestService.class);
        Object o = bean0.calculateTime();

        TestService bean1 = context.getBean(TestService.class);
        Object o1 = bean1.calculateTime();
        System.out.println(bean0.getClass());
        System.out.println(o);
        context.stop();
    }
}
