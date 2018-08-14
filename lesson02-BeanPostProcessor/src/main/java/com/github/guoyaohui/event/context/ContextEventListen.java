package com.github.guoyaohui.event.context;

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


    /**
     * bean的初始化已经完毕了
     */
    @EventListener
    public void contextRefreshedEventListener(ContextRefreshedEvent event) {
        System.out.println("ContextRefreshedEvent : 老子是演员，老子刚刚穿完了戏服");
    }

    @EventListener
    public void contextStartedEventListener(ContextStartedEvent event) {
        System.out.println("ContextStartedEvent : 演员开始念台词：我是谁，我在哪里，我要干嘛....");
    }

    @EventListener
    public void contextStoppedEventListener(ContextStoppedEvent event) {
        System.out.println("ContextStoppedEvent : 演员忘词了，还演个锤子");
    }

    @EventListener
    public void contextClosedEventListener(ContextClosedEvent event) {
        System.out.println("ContextClosedEvent : 演员借由中暑，先咔了");
    }
}
