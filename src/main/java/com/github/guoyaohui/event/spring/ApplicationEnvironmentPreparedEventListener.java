package com.github.guoyaohui.event.spring;

import java.util.Date;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author 郭垚辉
 * @date 2018/08/06
 */
public class ApplicationEnvironmentPreparedEventListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        System.out.println("ApplicationEnvironmentPreparedEventListener : " + new Date());
    }
}
