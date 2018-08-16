package com.github.guoyaohui.sepcial;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author 郭垚辉
 * @date 2018/08/16
 */
@Import(HelloWorld.class)
@Configuration
public class CustomImport {

    @Bean
    public NewWorld newWorld() {
        return new NewWorld();
    }

}
