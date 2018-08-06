package com.github.guoyaohui.event;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/06
 */
@Component
public class SpringEventListen {

    @EventListener
    public void applicationEnvironmentPreparedEventListen(ApplicationEnvironmentPreparedEvent event) {
        System.out.println(event);
    }

    @EventListener
    public void applicationPreparedEventListener(ApplicationPreparedEvent event) {
        System.out.println(event);
    }

    @EventListener
    public void applicationReadyEventListener(ApplicationReadyEvent event) {
        // ru
        System.out.println(event);
    }

    @EventListener
    public void applicationStartedEventListener(ApplicationStartedEvent event) {
        System.out.println(event);
    }

    @EventListener
    public void applicationFailedEventListener(ApplicationFailedEvent event) {
        System.out.println(event);
    }

    @EventListener
    public void applicationStartingEventListener(ApplicationStartingEvent event) {
        System.out.println(event);
    }
}
