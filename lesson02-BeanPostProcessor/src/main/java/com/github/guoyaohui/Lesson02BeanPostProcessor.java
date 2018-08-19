package com.github.guoyaohui;

import com.github.guoyaohui.bean.CustomBean01;
import com.github.guoyaohui.bean.CustomBean02;
import com.github.guoyaohui.bean.ICustomBean01;
import com.github.guoyaohui.bean.ICustomBean02;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author 郭垚辉
 * @create_time 2018/7/21
 */
@SpringBootApplication
//@EnableAspectJAutoProxy(proxyTargetClass = true)
public class Lesson02BeanPostProcessor {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Lesson02BeanPostProcessor.class);
        application.setBannerMode(Mode.OFF);

        ConfigurableApplicationContext context = application.run(args);
        // 发布context的start事件
        context.start();
        ICustomBean01 bean1 = context.getBean(ICustomBean01.class);
        ICustomBean02 bean2 = context.getBean(ICustomBean02.class);
        // 发布context的stop事件
        context.stop();
    }

}
