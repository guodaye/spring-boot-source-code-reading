package com.github.guoyaohui.core;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @create_time 2018/7/21
 */
@Configurable
@Component
public class TestComponent {


    @Bean
    public Map<String, String> params() {
        return new HashMap<>();
    }

}
