package com.github.guoyaohui.sepcial;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

/**
 * @author 郭垚辉
 * @date 2018/08/16
 */
@Import(HelloWorld.class)
@Component
public class CustomImport {

    @Bean
    public NewWorld newWorld() {
        return new NewWorld();
    }

}
