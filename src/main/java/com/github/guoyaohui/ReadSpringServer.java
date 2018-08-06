package com.github.guoyaohui;

import com.github.guoyaohui.event.spring.CustomApplicationEnvironmentPreparedEvent;
import com.github.guoyaohui.event.spring.CustomApplicationPreparedEvent;
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
        SpringApplication application = new SpringApplication(ReadSpringServer.class);
//        application.addListeners(new CustomApplicationEnvironmentPreparedEvent());
//        application.addListeners(new CustomApplicationPreparedEvent());

        ConfigurableApplicationContext context = application.run(args);

        // 触发ContextStartedEvent事件
        context.start();
        Thread.sleep(1000);
        // 触发ContextStoppedEvent
        context.stop();
    }

}
