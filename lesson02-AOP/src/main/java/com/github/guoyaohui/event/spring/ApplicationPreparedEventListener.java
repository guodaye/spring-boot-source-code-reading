package com.github.guoyaohui.event.spring;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author 郭垚辉
 * @date 2018/08/06
 */
public class ApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent> {

    /**
     * 放大招前准备背景音乐
     *
     * 准备环境
     * 加载配置文件
     * 加载配置信息
     */
    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        System.out.println("ApplicationPreparedEventListener : 灯光音效准备好了");
    }
}
