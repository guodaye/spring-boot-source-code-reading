package com.github.guoyaohui.event.spring;

import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author 郭垚辉
 * @date 2018/08/06
 */
public class ApplicationStartingEventListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        System.out.println("ApplicationStartingEventListener : 导演：东半球最好看的电视剧《蛋蛋哥》开拍辣");
    }
}
