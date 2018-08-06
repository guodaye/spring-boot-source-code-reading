package com.github.guoyaohui.event.context;

import java.util.Date;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/06
 */
@Component
public class ContextEventListen {

    @EventListener
    public void contextClosedEventListener(ContextClosedEvent event) {
        System.out.println("ContextClosedEvent : "+new Date());
    }

    @EventListener
    public void contextRefreshedEventListener(ContextRefreshedEvent event) {
        System.out.println("ContextRefreshedEvent : "+new Date());
    }

    @EventListener
    public void contextStoppedEventListener(ContextStoppedEvent event) {
        System.out.println("ContextStoppedEvent : "+new Date());
    }

    @EventListener
    public void contextStartedEventListener(ContextStartedEvent event) {
        System.out.println("ContextStartedEvent : "+new Date());
    }

}
