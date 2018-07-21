package com.github.guoyaohui.core;

import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @create_time 2018/7/21
 */

@Component
public class TestComponent2 {

    @Autowired
    private Map<String, String> params;

    @PostConstruct
    public void init() {
        System.out.println(params);
    }


}
