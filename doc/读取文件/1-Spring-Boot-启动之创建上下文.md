### 一、Spring-Boot启动的时候创建上下文

### 调用链看一切

当我们启动Spring的时候，这时候回去初始化一个AnnotationConfigApplicationContext的实例。

![](http://oimj9bzzz.bkt.clouddn.com/18-8-17/32867045.jpg)

初始化化时调用的是无参构造函数，我们通过方法调用关系可以看到最终会调用到AnnotaionConfigUtils.registerAnnotationConfigProcessor(BeanDefinitionRegistry)

![](http://oimj9bzzz.bkt.clouddn.com/18-8-17/28670129.jpg)

那么这个方法里面具体做了什么事情呢

```
public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(
      BeanDefinitionRegistry registry, Object source) {

   DefaultListableBeanFactory beanFactory = unwrapDefaultListableBeanFactory(registry);
   if (beanFactory != null) {
      // 设置依赖比较器，用于Order的注解的排序
      if (!(beanFactory.getDependencyComparator() instanceof AnnotationAwareOrderComparator)) {
         beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
      }
      // 设置自动依赖解析器为ContextAnnotationAutowireCandidateResolver
      if (!(beanFactory.getAutowireCandidateResolver() instanceof ContextAnnotationAutowireCandidateResolver)) {
         beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
      }
   }
   
   // 获取所有的BeanDefinitionHolder的集合
   Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<BeanDefinitionHolder>(4);

   // 设置ConfigurationClassPostProcessor，用于解析项目中注解，完成Java Config的注入
   if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
   }

   // 设置AutowiredAnnotationBeanPostProcessor，处理@Autowried , @Value，@Inject注解
   if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
   }
   // 设置RequiredAnnotationBeanPostProcessor，处理@Required注解
   if (!registry.containsBeanDefinition(REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(RequiredAnnotationBeanPostProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
   }
   // 判断JSR-250标准：如果存在javax.annotation.Resource就表示是JSR-250标准
   // 检测是否符合JSR-250标准，如果存在则设置CommonAnnotationBeanPostProcessor，处理
   // @PostConstruct，@PreDestory，@Resource，@WebServiceRef注解。
   if (jsr250Present && !registry.containsBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
   }


   // 判断是否存在JPA：同时存在javax.persistence.EntityManagerFactory和
   // org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor两个类，表示存在实体类
   // 如果存在JPA，则PersistenceAnnotationBeanPostProcessor注入，以处理JPA的操作
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
   // 设置EventListenerMethodProcessor，处理@EventListener注解
   if (!registry.containsBeanDefinition(EVENT_LISTENER_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(EventListenerMethodProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_PROCESSOR_BEAN_NAME));
   }
   
   // 设置DefaultEventListenerFactory，用于创建监听到@EventListener的方法为
   // ApplicationListener
   if (!registry.containsBeanDefinition(EVENT_LISTENER_FACTORY_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(DefaultEventListenerFactory.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_FACTORY_BEAN_NAME));
   }

   // 返回所有的BeanDefinition
   return beanDefs;
}
```
为什么Spring能达到自动注入的根本原因也在这里。