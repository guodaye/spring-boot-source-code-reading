### 一、spring boot事件传播

spring中的事件传播的监听器可以通过两种方式来进行监听

1. `@EventListener`
2. `SpringApplication.addListeners()`
3. `META-INF/spring.factories `



下面我们来谈谈三种的使用方式

##### 第一种

使用的前提必须是添加了该注解的方法必须首先注册为`Bean`，类似下面这样子

```
@Component
public class ContextEventListen {

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
```

##### 第二种

由于有些事件在`ApplicationContext`初始化前就发生了，这时候就无法使用`@EventListener`注解了。 

这时候，我们就需要手动的在main函数中添加监听器，以保证程序执行时，发布对应事件可以被监听到。

首先，我们定义了一个事件监听器

```
public class ApplicationStartingEventListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        System.out.println("ApplicationStartingEventListener : 导演：东半球最好看的电视剧《蛋蛋哥》开拍辣");
    }
}
```

在main函数中进行注册，必须要在run执行前完成添加

```
@SpringBootApplication
public class Lesson01Application {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Lesson01Application.class);
        application.addListeners(new ApplicationStartingEventListener());
        ConfigurableApplicationContext context = application.run(args);
        context.stop();
    }

}
```

##### 第三种

有时候，我们不愿意 在main中添加多余的代码。这时候，我们可以使用spring boot提供的spring.factories文件来为我们提前完成监听器的注册。

首先，和第二种一样，首先定义事件监听器

```
public class ApplicationStartingEventListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        System.out.println("ApplicationStartingEventListener : 导演：东半球最好看的电视剧《蛋蛋哥》开拍辣");
    }
}
```

在resources目录下创建文件/META-INF/spring.factories文件，添加如下内容

```
org.springframework.context.ApplicationListener=\
  com.github.guoyaohui.event.spring.ApplicationStartingEventListener
```

在启动时，spring-boot会自动为我们添加该监听器。



#### 执行结果

```
ApplicationStartingEventListener : 导演：东半球最好看的电视剧《蛋蛋哥》开拍辣
ApplicationEnvironmentPreparedEventListener : 放大招前准备背景音乐
ApplicationPreparedEventListener : 灯光音效准备好了
ContextRefreshedEvent : 老子是演员，老子刚刚穿完了戏服
ApplicationStartedEvent : 导演：演员准备就绪，准备开拍
ApplicationReadyEvent : 大家准备好：action！！！
ContextStartedEvent : 演员开始念台词：我是谁，我在哪里，我要干嘛....
ContextStoppedEvent : 演员忘词了，还演个锤子
ContextClosedEvent : 演员借由中暑，先咔了
```

### 总结

1. 目前我发现了已经有三种事件是无法使用`@EventListener`注解

```
ApplicationStartingEvent
ApplicationEnvironmentPreparedEvent
ApplicationPreparedEvent
```

2. 如果需要监听到`ContextStartedEvent`事件需要在main函数中调用start方法

```
@SpringBootApplication
public class Lesson01Application {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Lesson01Application.class);
        application.setBannerMode(Mode.OFF);
        ConfigurableApplicationContext context = application.run(args);
        // 发布ContextStartedEvent事件
        context.start();

        // 发布context的stop事件
        context.stop();
    }

}
```



#### 参考地址：

1. [ 23.5 Application events and listeners](https://docs.spring.io/spring-boot/docs/1.5.15.RELEASE/reference/htmlsingle/)

