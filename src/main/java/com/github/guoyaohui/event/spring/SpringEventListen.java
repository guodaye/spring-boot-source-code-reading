package com.github.guoyaohui.event.spring;

import java.util.Date;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
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
    public void applicationEnvironmentPreparedEventListen(ApplicationEnvironmentPreparedEvent event) {
        System.out.println("ApplicationEnvironmentPreparedEvent : " + new Date());
    }

    @EventListener
    public void applicationPreparedEventListener(ApplicationPreparedEvent event) {
        System.out.println("ApplicationPreparedEvent : " + new Date());
    }

    @EventListener
    public void applicationReadyEventListener(ApplicationReadyEvent event) {
        System.out.println("ApplicationReadyEvent : " + new Date());
    }

    @EventListener
    public void applicationStartedEventListener(ApplicationStartedEvent event) {
        System.out.println("ApplicationStartedEvent : " + new Date());
    }

    @EventListener
    public void applicationFailedEventListener(ApplicationFailedEvent event) {
        System.out.println("ApplicationFailedEvent : " + new Date());
    }

    @EventListener
    public void applicationStartingEventListener(ApplicationStartingEvent event) {
        System.out.println("ApplicationStartingEvent : " + new Date());
    }
}
