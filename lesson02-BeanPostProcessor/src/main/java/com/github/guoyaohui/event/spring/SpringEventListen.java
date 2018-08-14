package com.github.guoyaohui.event.spring;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * https://stackoverflow.com/questions/38487474/springboot-eventlistener-dont-receive-events
 * @author 郭垚辉
 * @date 2018/08/06
 */
@Component
public class SpringEventListen {

    @EventListener
    public void applicationStartedEventListener(ApplicationStartedEvent event) {
        System.out.println("ApplicationStartedEvent : 导演：演员准备就绪，准备开拍");
    }

    @EventListener
    public void applicationReadyEventListener(ApplicationReadyEvent event) {
        System.out.println("ApplicationReadyEvent : 大家准备好：action！！！");
    }



    @EventListener
    public void applicationFailedEventListener(ApplicationFailedEvent event) {
        System.out.println("ApplicationFailedEvent : 天有不测风云，演员被台风吹跑了，杀青");
    }
}
