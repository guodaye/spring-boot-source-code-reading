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



### 三、DefaultListableBeanFactory

以下是DefaultListableBeanFactory的变量

```
// key为工厂实例的id， value为对应的工厂实例
private static final Map<String, Reference<DefaultListableBeanFactory>> serializableFactories =
      new ConcurrentHashMap<>(8);

// beanFactory的id（可选）
@Nullable
private String serializationId;

// 有什么用？
// 是否允许一个同名的不同的BeanDefinition进行重复注册
private boolean allowBeanDefinitionOverriding = true;

// 是否允许懒加载的bean进行提前初始化
private boolean allowEagerClassLoading = true;

// 顺序比较器，用于比较Bean的加载先后顺序
@Nullable
private Comparator<Object> dependencyComparator;

// 检查BeanDefinition是否为自动注入的类型解析器
private AutowireCandidateResolver autowireCandidateResolver = new SimpleAutowireCandidateResolver();

// 保存bean的Class和自动注入的值的map集合
private final Map<Class<?>, Object> resolvableDependencies = new ConcurrentHashMap<>(16);

// key为bean的name，value为BeanDifinition的集合
private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

// key为bean的类型，value为bean的名字。这里保存的是单例和非单例所有的bean的集合
private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<>(64);

//  key为bean的类型，value为bean的名字。只存单例的bean的集合
private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<>(64);

// 按照BeanDifinition的注册顺序，将注册的BeanDifinition的名字保存在该集合中
private volatile List<String> beanDefinitionNames = new ArrayList<>(256);

// 按照BeanDifinition的注册顺序，手动注册的BeanDifinition的名字的集合
private volatile Set<String> manualSingletonNames = new LinkedHashSet<>(16);

// 当configurationFrozen为true时，会缓存所有的BeanDifinition的名字
@Nullable
private volatile String[] frozenBeanDefinitionNames;

// 是否将所有的Bean的BeanDifinition都进行缓存
private volatile boolean configurationFrozen = false;
```



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
      // 配置
      // 1. 部分的BeanPostProcessor
      // 2. 需要忽略的接口
      // 3. 需要注入一些bean
      prepareRefresh();

      // Tell the subclass to refresh the internal bean factory.
      ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

      // Prepare the bean factory for use in this context.
      prepareBeanFactory(beanFactory);

      try {
         // 空实现
         postProcessBeanFactory(beanFactory);

         // 注册BeanFactoryPostProcessor
         // 应该是对FactoryBean的后置处理器
         invokeBeanFactoryPostProcessors(beanFactory);

         // 注册BeanPostProcessor
         // 拦截bean的后置处理器
         registerBeanPostProcessors(beanFactory);

         // 初始化message source
         initMessageSource();

         // 注册SimpleApplicationEventMulticaster
         // 发布
         initApplicationEventMulticaster();

         // Initialize other special beans in specific context subclasses.
         onRefresh();

         // 检查并注册所有的监听器Bean
         registerListeners();
         
         // 实例化所有非懒加载的单例(包括普通的Bean和FactoryBean)
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

####  1. 准备BeanFactoryAbstractApplicationContext#prepareBeanFactory

```
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
   // 设置BeanFactory的ClassLoader
   beanFactory.setBeanClassLoader(getClassLoader());
   
   // 设置Bean的表达式解析处理器
   beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
   
   // 设置Bean的属性编辑注册器
   beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

   // 将ApplicationContextAwareProcessor作为一个BeanPostProcessor
   beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
   
   // beanFactory中忽略的依赖的接口，也就是根据这些Class，无法获取到对应的Bean实例
   beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
   beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
   beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
   beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
   beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
   beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

   // 注册啥，我也不太清楚
   beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
   beanFactory.registerResolvableDependency(ResourceLoader.class, this);
   beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
   beanFactory.registerResolvableDependency(ApplicationContext.class, this);

   // 添加了BeanPostProcessor
   beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

   // 以下为常规操作注册
   if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
      beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
      beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
   }

   if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
      beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
   }
   
   if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
      beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
   }
   
   if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
      beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
   }
}
```



#### 2.AbstractApplicationContext#invokeBeanFactoryPostProcessors 

如果`BeanPostProcessor`是有顺序的，则先进行排序，而后实例化所有注册的`BeanFactoryPostProcessor`的实例。这个方法必须在实例化所有的Bean之前被调用。

```
protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
   // getBeanFactoryPostProcessors() --> 获取到已经实例化的所有BeanFactoryPostProcessor
   PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

   // 以下不理解作用
   if (beanFactory.getTempClassLoader() == null && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
      beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
      beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
   }
}
```



##### 2.1. PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors

```
// beanFactoryPostProcessors是已经实例化过的BeanFactoryPostProcessor
public static void invokeBeanFactoryPostProcessors(
      ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
   Set<String> processedBeans = new HashSet<>();

   if (beanFactory instanceof BeanDefinitionRegistry) {
      BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
      List<BeanFactoryPostProcessor> regularPostProcessors = new LinkedList<>();
      List<BeanDefinitionRegistryPostProcessor> registryProcessors = new LinkedList<>();
      for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
         if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
            BeanDefinitionRegistryPostProcessor registryProcessor =
                  (BeanDefinitionRegistryPostProcessor) postProcessor;
            registryProcessor.postProcessBeanDefinitionRegistry(registry);
            registryProcessors.add(registryProcessor);
         }
         else {
            regularPostProcessors.add(postProcessor);
         }
      }

      List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();
      String[] postProcessorNames =
            beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
      for (String ppName : postProcessorNames) {
         if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
            currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
            processedBeans.add(ppName);
         }
      }
      
      sortPostProcessors(currentRegistryProcessors, beanFactory);
      registryProcessors.addAll(currentRegistryProcessors);
      invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
      currentRegistryProcessors.clear();

      // 其次，我们来执行获取实现了Ordered接口和BeanDefinitionRegistryPostProcessor接口的
      // BeanFactoryPostProcessor的实例集合的名字
      postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
      for (String ppName : postProcessorNames) {
         if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
            currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
            processedBeans.add(ppName);
         }
      }
      sortPostProcessors(currentRegistryProcessors, beanFactory);
      registryProcessors.addAll(currentRegistryProcessors);
      invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
      currentRegistryProcessors.clear();
      boolean reiterate = true;
      while (reiterate) {
         reiterate = false;
         postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
         for (String ppName : postProcessorNames) {
            if (!processedBeans.contains(ppName)) {
               currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
               processedBeans.add(ppName);
               reiterate = true;
            }
         }
         sortPostProcessors(currentRegistryProcessors, beanFactory);
         registryProcessors.addAll(currentRegistryProcessors);
         invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
         currentRegistryProcessors.clear();
      }
      
      invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
      invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
   }

   else {
      // 执行postProcessBeanFactory方法
      invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
   }

   // Do not initialize FactoryBeans here: We need to leave all regular beans
   // uninitialized to let the bean factory post-processors apply to them!
   // 这些获取的只是一些BeanFactoryPostProcessor存在BeanFactory中还是以BeanDefinition的形式存在，
   // 但是没有进行实例化的Bean
   String[] postProcessorNames =
         beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

   // Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
   // Ordered, and the rest.
   List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
   List<String> orderedPostProcessorNames = new ArrayList<>();
   List<String> nonOrderedPostProcessorNames = new ArrayList<>();
   for (String ppName : postProcessorNames) {
      if (processedBeans.contains(ppName)) {
         // skip - already processed in first phase above
      }
      else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
         priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
      }
      else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
         orderedPostProcessorNames.add(ppName);
      }
      else {
         nonOrderedPostProcessorNames.add(ppName);
      }
   }

   // First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
   sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
   invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

   // Next, invoke the BeanFactoryPostProcessors that implement Ordered.
   List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
   for (String postProcessorName : orderedPostProcessorNames) {
      orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
   }
   sortPostProcessors(orderedPostProcessors, beanFactory);
   invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

   // Finally, invoke all other BeanFactoryPostProcessors.
   List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
   for (String postProcessorName : nonOrderedPostProcessorNames) {
      nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
   }
   invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

   // Clear cached merged bean definitions since the post-processors might have
   // modified the original metadata, e.g. replacing placeholders in values...
   beanFactory.clearMetadataCache();
}
```

###### 第一种情况：实现`BeanDefinitionRegistry`接口

如果`ConfigurableListableBeanFactory`实现了`BeanDefinitionRegistry`接口，会按照下面的步骤进行处

1. 实现`PriorityOrdered`接口，排序后执行：`BeanDefinitionRegistryPostProcessor`#`postProcessBeanDefinitionRegistry`的方法
2. 实现`Ordered`接口，排序后执行：`BeanDefinitionRegistryPostProcessor`#`postProcessBeanDefinitionRegistry`
3. 对剩余的`BeanDefinitionRegistryPostProcessor`，进行排序后执行`BeanDefinitionRegistryPostProcessor`#`postProcessBeanDefinitionRegistry`
4. 执行`BeanDefinitionRegistryPostProcessor`的集合执行`BeanDefinitionRegistryPostProcessor`#`postProcessBeanFactory`
5. 执行`BeanFactoryPostProcessor`的集合执行`BeanFactoryPostProcessor`#`postProcessBeanFactory`
6. 此后还会继续获取`BeanFactoryPostProcessor`的类，如果这个类之前执行过就不会再继续执行了，只有这个类没有执行过，这时候会进行排序，依次按顺序调用执行`postProcessBeanFactory`方法。

###### 第二种情况：没有实现`BeanDefinitionRegistry`接口（怎么进入这个分支？？？）

1. 直接执行传入的`BeanFactoryPostProcessor`的集合的`postProcessBeanFactory`方法
2. 此后还会继续获取`BeanFactoryPostProcessor`的类，然后对获取的类进行排序，这时候会进行排序，依次按顺序调用执行`postProcessBeanFactory`方法。
3. 其实对于这个分支，有一个疑问，不会出现重复执行吗？？？还是虽然会重复执行，但是后面的执行步骤可以将前面错误的执行顺序进行修正呢？？？**好奇？？？**

| RegistryBeanPostProcessor的Registry类              | 作用 |
| -------------------------------------------------- | ---- |
| SharedMetadataReaderFactoryBean                    |      |
| ConfigurationWarningsApplicationContextInitializer |      |

#### 3. AbstractApplicationContext#registerBeanPostProcessors

我们通过这个方法可以看到，还是通过代理注册器来往`BeanFactory`中添加`BeanPostProcessor`

```
protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
   PostProcessorRegistrationDelegate.registerBeanPostProcessors(beanFactory, this);
}
```

##### 3.1 PostProcessorRegistrationDelegate#registerBeanPostProcessors 

```
public static void registerBeanPostProcessors(
      ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

   String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

   // Register BeanPostProcessorChecker that logs an info message when
   // a bean is created during BeanPostProcessor instantiation, i.e. when
   // a bean is not eligible for getting processed by all BeanPostProcessors.
   int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
   beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

   // Separate between BeanPostProcessors that implement PriorityOrdered,
   // Ordered, and the rest.
   List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
   List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
   List<String> orderedPostProcessorNames = new ArrayList<>();
   List<String> nonOrderedPostProcessorNames = new ArrayList<>();
   for (String ppName : postProcessorNames) {
      if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
         BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
         priorityOrderedPostProcessors.add(pp);
         if (pp instanceof MergedBeanDefinitionPostProcessor) {
            internalPostProcessors.add(pp);
         }
      }
      else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
         orderedPostProcessorNames.add(ppName);
      }
      else {
         nonOrderedPostProcessorNames.add(ppName);
      }
   }

   // First, register the BeanPostProcessors that implement PriorityOrdered.
   sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
   registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

   // Next, register the BeanPostProcessors that implement Ordered.
   List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
   for (String ppName : orderedPostProcessorNames) {
      BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
      orderedPostProcessors.add(pp);
      if (pp instanceof MergedBeanDefinitionPostProcessor) {
         internalPostProcessors.add(pp);
      }
   }
   sortPostProcessors(orderedPostProcessors, beanFactory);
   registerBeanPostProcessors(beanFactory, orderedPostProcessors);

   // Now, register all regular BeanPostProcessors.
   List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
   for (String ppName : nonOrderedPostProcessorNames) {
      BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
      nonOrderedPostProcessors.add(pp);
      if (pp instanceof MergedBeanDefinitionPostProcessor) {
         internalPostProcessors.add(pp);
      }
   }
   registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

   // Finally, re-register all internal BeanPostProcessors.
   sortPostProcessors(internalPostProcessors, beanFactory);
   registerBeanPostProcessors(beanFactory, internalPostProcessors);

   // Re-register post-processor for detecting inner beans as ApplicationListeners,
   // moving it to the end of the processor chain (for picking up proxies etc).
   beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
}
```

对于`BeanPostProcessor`的实例化过程和 `invokeBeanFactoryPostProcessor`的过程类似，也是首先通过获取`BeanFactory`中`Class`为`BeanPostProcessor的BeanName`，然后再通过`PriorityOrdered`的接口，`Ordered`接口进行按顺序进行排序，并进行初始化。



#### 6. AbstractApplicationContext#finishBeanFactoryInitialization

在这个过程中，会对spring没有设置为懒加载的Bean和FactoryBean进行实例化。

