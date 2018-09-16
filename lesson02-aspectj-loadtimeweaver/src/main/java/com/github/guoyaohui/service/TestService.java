package com.github.guoyaohui.service;

import org.springframework.stereotype.Service;

/**
 * @author 郭垚辉
 * @date 2018/9/16
 */
@Service
public class TestService {


    public Object calculateTime() throws InterruptedException {
        Thread.sleep(5000);
        return "hello world";
    }
}
