### 一、ApplicationContext和BeanFactory的关系

#### 1.1 BeanFactory接口

```
package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

public interface BeanFactory {

   // 工厂类bean的前缀
   String FACTORY_BEAN_PREFIX = "&";
   // 通过bean的name获取bean实例
   Object getBean(String name) throws BeansException;
   // 通过bean的name和给定的Class类获取bean实例
   <T> T getBean(String name, @Nullable Class<T> requiredType) throws BeansException;
   // 按名字和显示声明的参数来创建实例，注意显示声明的参数是用来创建原型的
   Object getBean(String name, Object... args) throws BeansException;
   // 按照给定类型来获取bean实例
   <T> T getBean(Class<T> requiredType) throws BeansException;
   // 按名字和显示声明的参数来创建实例，注意显示声明的参数是用来创建原型的
   <T> T getBean(Class<T> requiredType, Object... args) throws BeansException;
   // 判断当前的beanfactory是否包含给定的name的bean
   boolean containsBean(String name);
   // 判断给定的name的bean是不是单例的
   boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

   // 判断一个bean是不是prototype类型
   boolean isPrototype(String name) throws NoSuchBeanDefinitionException;
   // 判断bean的name和给定的Class是否匹配
   boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;

   // 判断bean的name和给定的Class是否匹配
   boolean isTypeMatch(String name, @Nullable Class<?> typeToMatch) throws NoSuchBeanDefinitionException;

   // 根据给定的bean的name，获取到他的Class类型
   @Nullable
   Class<?> getType(String name) throws NoSuchBeanDefinitionException;

   // 根据给定的bean的名字，获取他的别名
   String[] getAliases(String name);

}
```



#### 1.2 ApplicationContext接口

```
package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;

public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
      MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

   // 获取当前applicationContext的id
   @Nullable
   String getId();

   // 获取当前的ApplicationContext的名字，默认为空
   String getApplicationName();

   // 获取当前的ApplicationContext的显示名字，默认不为空
   // eg：org.springframework.context.annotation.AnnotationConfigApplicationContext@65b3f4a4
   String getDisplayName();

   // 获取ApplicationContext的启动时间
   long getStartupDate();

   // 获取当前ApplicationContext的父级ApplicationContext
   @Nullable
   ApplicationContext getParent();

   // 获取有自动注入能力的BeanFactory
   AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}
```

从接口的继承看来，我们可以总结出来ApplicationContext所具备的能力有：

1. `EnvironmentCapable`：配置环境的能力，例如profile环境
2. `ListableBeanFactory`：枚举bean定义的能力的
3. `MessageSource`：国际化应用的能力(用的较少， 以后再进行深入理解)
4. `ApplicationEventPublisher`：发布事件的能力
5. `ResourcePatternResolver`：加载资源文件的能力



通过两个接口的比较，我们可以很明显的得到一个结论便是：Bean的相关操作都是由`BeanFactory`及其子类完成的，而`ApplicationContext`则包装了`BeanFactory`，它将`BeanFactory`纳入旗下，借助`BeanFactory`的能力，又修炼了其他的能力，驰骋沙场之中。



### 二、spring-boot中的自动注入的ApplicationContext

#### 2.1 建立上下文

```
protected ConfigurableApplicationContext createApplicationContext() {
   Class<?> contextClass = this.applicationContextClass;
   if (contextClass == null) {
      try {
         switch (this.webApplicationType) {
         case SERVLET:
            contextClass = Class.forName(DEFAULT_WEB_CONTEXT_CLASS);
            break;
         case REACTIVE:
            contextClass = Class.forName(DEFAULT_REACTIVE_WEB_CONTEXT_CLASS);
            break;
         default:
            contextClass = Class.forName(DEFAULT_CONTEXT_CLASS);
         }
      }
      catch (ClassNotFoundException ex) {
         throw new IllegalStateException(
               "Unable create a default ApplicationContext, "
                     + "please specify an ApplicationContextClass",
               ex);
      }
   }
   return (ConfigurableApplicationContext) BeanUtils.instantiateClass(contextClass);
}
```



普通的servlet应用使用的上下文

```
org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext
```

普通的java应用使用的上下文

```
org.springframework.context.annotation.AnnotationConfigApplicationContext
```

响应式的web应用的上下文

```
org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext
```

在这里，我们不进行`AnnotationConfigReactiveWebServerApplicationContext`的分析。

#### 2.2 AnnotationConfigServletWebServerApplicationContext和AnnotationConfigApplicationContext的比较



这是`AnnotationConfigServletWebServerApplicationContext`的继承关系

![](http://oimj9bzzz.bkt.clouddn.com/18-8-14/56797341.jpg)

而这是`AnnotationConfigApplicationContext`的继承关系

![](http://oimj9bzzz.bkt.clouddn.com/18-8-14/97053842.jpg)

通过上面两个`ApplicationContext`的层级关系，我们注意到他们都继承了`GenericAppliationContext`。我们知道在Java中，调用子类的构造函数的时候，首先会先去调用父类的无参构造函数。也就是说，子类的构造函数少了一行`super()`。

这时候，我们来看看`GenericApplicationContext`的构造函数

```
public GenericApplicationContext() {
   this.beanFactory = new DefaultListableBeanFactory();
}
```

也就是说，上述的两个`ApplicationContext`的中使用的`BeanFactoy`都是`DefaultListableBeanFactory`



### 三、注入AnnotationConfigProcessor

这是在`org.springframework.context.annotation.AnnotationConfigUtils`#`registerAnnotationConfigProcessors`

```
public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(
      BeanDefinitionRegistry registry, @Nullable Object source) {

   DefaultListableBeanFactory beanFactory = unwrapDefaultListableBeanFactory(registry);
   if (beanFactory != null) {
      if (!(beanFactory.getDependencyComparator() instanceof AnnotationAwareOrderComparator)) {
         beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
      }
      if (!(beanFactory.getAutowireCandidateResolver() instanceof ContextAnnotationAutowireCandidateResolver)) {
         beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
      }
   }

   Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<>(4);

   if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
   }

   if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
   }

   if (!registry.containsBeanDefinition(REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(RequiredAnnotationBeanPostProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
   }

   // Check for JSR-250 support, and if present add the CommonAnnotationBeanPostProcessor.
   if (jsr250Present && !registry.containsBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
   }

   // Check for JPA support, and if present add the PersistenceAnnotationBeanPostProcessor.
   if (jpaPresent && !registry.containsBeanDefinition(PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition();
      try {
         def.setBeanClass(ClassUtils.forName(PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME,
               AnnotationConfigUtils.class.getClassLoader()));
      }
      catch (ClassNotFoundException ex) {
         throw new IllegalStateException(
               "Cannot load optional framework class: " + PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME, ex);
      }
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME));
   }

   if (!registry.containsBeanDefinition(EVENT_LISTENER_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(EventListenerMethodProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_PROCESSOR_BEAN_NAME));
   }
   if (!registry.containsBeanDefinition(EVENT_LISTENER_FACTORY_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(DefaultEventListenerFactory.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_FACTORY_BEAN_NAME));
   }

   return beanDefs;
}
```

当该方法执行过后，我们通过断点获取beanDefs

![](http://oimj9bzzz.bkt.clouddn.com/18-8-14/81607395.jpg)



### 三、Bean的实例化

这个方法是在`AbstractApplicationContext`中定义的，上面的两个容器进行refresh都会通过super.refresh()调用该方法，也只有在这个方法中才会去做所有的Bean的实例化

```
@Override
public void refresh() throws BeansException, IllegalStateException {
   // 加锁防止多线程重复实例化Bean
   // 问题是在哪里会发生多线程的实例化Bean，担心用户瞎操作吗
   synchronized (this.startupShutdownMonitor) {
      // Prepare this context for refreshing.
      prepareRefresh();

      // Tell the subclass to refresh the internal bean factory.
      ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

      // Prepare the bean factory for use in this context.
      prepareBeanFactory(beanFactory);

      try {
         // Allows post-processing of the bean factory in context subclasses.
         postProcessBeanFactory(beanFactory);

         // Invoke factory processors registered as beans in the context.
         invokeBeanFactoryPostProcessors(beanFactory);

         // Register bean processors that intercept bean creation.
         registerBeanPostProcessors(beanFactory);

         // Initialize message source for this context.
         initMessageSource();

         // Initialize event multicaster for this context.
         initApplicationEventMulticaster();

         // Initialize other special beans in specific context subclasses.
         onRefresh();

         // Check for listener beans and register them.
         registerListeners();

         // Instantiate all remaining (non-lazy-init) singletons.
         finishBeanFactoryInitialization(beanFactory);

         // Last step: publish corresponding event.
         finishRefresh();
      }

      catch (BeansException ex) {
         if (logger.isWarnEnabled()) {
            logger.warn("Exception encountered during context initialization - " +
                  "cancelling refresh attempt: " + ex);
         }

         // Destroy already created singletons to avoid dangling resources.
         destroyBeans();

         // Reset 'active' flag.
         cancelRefresh(ex);

         // Propagate exception to caller.
         throw ex;
      }

      finally {
         // Reset common introspection caches in Spring's core, since we
         // might not ever need metadata for singleton beans anymore...
         resetCommonCaches();
      }
   }
}
```