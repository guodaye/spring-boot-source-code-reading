package com.github.guoyaohui;

import com.github.guoyaohui.bean.ICustomBean01;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

/**
 * @author 郭垚辉
 * @create_time 2018/7/21
 */
@ImportResource("beans.xml")
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class Lesson02AOP {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Lesson02AOP.class);
        application.setBannerMode(Mode.OFF);

        ConfigurableApplicationContext context = application.run(args);
        // 发布context的start事件
        context.start();
        ICustomBean01 bean1 = context.getBean(ICustomBean01.class);
        // 发布context的stop事件
        String value = bean1.getValue();
        System.out.println(value);
        context.stop();
    }

}
