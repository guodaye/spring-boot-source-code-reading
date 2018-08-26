package com.github.guoyaohui;

import com.github.guoyaohui.bean.CustomBean;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author 郭垚辉
 * @create_time 2018/7/21
 */
@SpringBootApplication
public class Lesson01Bean {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Lesson01Bean.class);
        application.setBannerMode(Mode.OFF);

        ConfigurableApplicationContext context = application.run(args);
        // 发布context的start事件
        context.start();
        CustomBean customBean0 = context.getBean(CustomBean.class);
        CustomBean customBean1 = context.getBean(CustomBean.class);
        CustomBean customBean2 = context.getBean(CustomBean.class);
        CustomBean customBean3 = context.getBean(CustomBean.class);
        CustomBean customBean4 = context.getBean(CustomBean.class);
        // 发布context的stop事件
        context.stop();
    }

}
