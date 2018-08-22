1. BeanDefinition分析
  MergedBeanDefinitionPostProcessor ==> postProcessMergedBeanDefinition
  2.
  InstantiationAwareBeanPostProcessor ==> postProcessAfterInstantiation

2. @Autowired @Value处理
  InstantiationAwareBeanPostProcessor ==> postProcessPropertyValues

  

3. 检查需要依赖的属性 @DependOns
  checkDependencies

  

4. 注入属性值
  applyPropertyValues

5. 处理 BeanNameAware  BeanClassLoaderAware BeanFactoryAware
  invokeAwareMethods

6. 
  postProcessBeforeInitialization

7.1 
ApplicationContextAwareProcessor ==> 处理Aware
invokeAwareInterfaces
EnvironmentAware
EmbeddedValueResolverAware
ResourceLoaderAware
ApplicationEventPublisherAware
MessageSourceAware
ApplicationContextAware

7.2 ConfigurationClassPostProcessor ==> 处理ImportAware
7.3 PostProcessorRegistrationDelegate == donothing
7.4 ConfigurationPropertiesBindingPostProcessor ==> 处理ConfigurationProperties注解
7.5 AbstractAutoProxyCreator ==> donothingm 做代理类
7.6 InitDestroyAnnotationBeanPostProcessor ==> 执行@PostConstruct标注的注解
7.7 InstantiationAwareBeanPostProcessorAdapter == donothing
7.8 InstantiationAwareBeanPostProcessorAdapter 

8.
InitializingBean

9.
postProcessAfterInitialization

ApplicationListenerDetector == 添加监听器ApplicationListener

10. 
    registerDisposableBeanIfNecessary注册@PreDestory事件