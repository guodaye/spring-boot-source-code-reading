### 一、两个注解



#### 1.1 @Import

```
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import {

   /**
    * {@link Configuration}, {@link ImportSelector}, {@link ImportBeanDefinitionRegistrar}
    * or regular component classes to import.
    */
   Class<?>[] value();

}
```

用于将整个Java Class导入到Spring的容器中

#### 1.2 @ImportResource

```
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ImportResource {


   @AliasFor("locations")
   String[] value() default {};


   @AliasFor("value")
   String[] locations() default {};


   Class<? extends BeanDefinitionReader> reader() default BeanDefinitionReader.class;

}
```

用于将xml文件导入到Spring的容器中



### 二、实现原理 

### 2.1 解析注解

**ConfigurationClassParser#doProcessConfigurationClass**

```
protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass)
      throws IOException {

   // Recursively process any member (nested) classes first
   processMemberClasses(configClass, sourceClass);

   // 处理@PropertySource annotations
   for (AnnotationAttributes propertySource : AnnotationConfigUtils.attributesForRepeatable(
         sourceClass.getMetadata(), PropertySources.class,
         org.springframework.context.annotation.PropertySource.class)) {
      if (this.environment instanceof ConfigurableEnvironment) {
         processPropertySource(propertySource);
      }
      else {
         logger.warn("Ignoring @PropertySource annotation on [" + sourceClass.getMetadata().getClassName() +
               "]. Reason: Environment must implement ConfigurableEnvironment");
      }
   }

   // 处理@ComponentScan
   Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
         sourceClass.getMetadata(), ComponentScans.class, ComponentScan.class);
   if (!componentScans.isEmpty() &&
         !this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
      for (AnnotationAttributes componentScan : componentScans) {
         // The config class is annotated with @ComponentScan -> perform the scan immediately
         Set<BeanDefinitionHolder> scannedBeanDefinitions =
               this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
         // Check the set of scanned definitions for any further config classes and parse recursively if needed
         for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
            if (ConfigurationClassUtils.checkConfigurationClassCandidate(
                  holder.getBeanDefinition(), this.metadataReaderFactory)) {
               parse(holder.getBeanDefinition().getBeanClassName(), holder.getBeanName());
            }
         }
      }
   }

   // 处理@Import
   processImports(configClass, sourceClass, getImports(sourceClass), true);

   // Process any @ImportResource annotations
   if (sourceClass.getMetadata().isAnnotated(ImportResource.class.getName())) {
      AnnotationAttributes importResource =
            AnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), ImportResource.class);
      String[] resources = importResource.getStringArray("locations");
      Class<? extends BeanDefinitionReader> readerClass = importResource.getClass("reader");
      for (String resource : resources) {
         String resolvedResource = this.environment.resolveRequiredPlaceholders(resource);
         configClass.addImportedResource(resolvedResource, readerClass);
      }
   }

   // 处理@Bean
   Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
   for (MethodMetadata methodMetadata : beanMethods) {
      configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
   }

   // Process default methods on interfaces
   processInterfaces(configClass, sourceClass);

   // Process superclass, if any
   if (sourceClass.getMetadata().hasSuperClass()) {
      String superclass = sourceClass.getMetadata().getSuperClassName();
      if (!superclass.startsWith("java") && !this.knownSuperclasses.containsKey(superclass)) {
         this.knownSuperclasses.put(superclass, configClass);
         // Superclass found, return its annotation metadata and recurse
         return sourceClass.getSuperClass();
      }
   }

   // No superclass -> processing is complete
   return null;
}
```


##### 2.1.1 处理@ComponentScan

**2.1.1.1 ComponentScanAnnotationParser** 

解析@ComponentScan中的属性，并进行扫描

```
public Set<BeanDefinitionHolder> parse(AnnotationAttributes componentScan, final String declaringClass) {
   Assert.state(this.environment != null, "Environment must not be null");
   Assert.state(this.resourceLoader != null, "ResourceLoader must not be null");

   // 获取一个BeanDifinition的扫描器
   ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(this.registry,
         componentScan.getBoolean("useDefaultFilters"), this.environment, this.resourceLoader);

   Class<? extends BeanNameGenerator> generatorClass = componentScan.getClass("nameGenerator");
   boolean useInheritedGenerator = (BeanNameGenerator.class == generatorClass);
   scanner.setBeanNameGenerator(useInheritedGenerator ? this.beanNameGenerator :
         BeanUtils.instantiateClass(generatorClass));

   // 获取Bean的代理模式
   // 不代理；CGLIB；JDK动态代理
   ScopedProxyMode scopedProxyMode = componentScan.getEnum("scopedProxy");
   if (scopedProxyMode != ScopedProxyMode.DEFAULT) {
      scanner.setScopedProxyMode(scopedProxyMode);
   }
   else {
      Class<? extends ScopeMetadataResolver> resolverClass = componentScan.getClass("scopeResolver");
      scanner.setScopeMetadataResolver(BeanUtils.instantiateClass(resolverClass));
   }

   scanner.setResourcePattern(componentScan.getString("resourcePattern"));
   // 判断是否有指定要进行扫描的类
   for (AnnotationAttributes filter : componentScan.getAnnotationArray("includeFilters")) {
      for (TypeFilter typeFilter : typeFiltersFor(filter)) {
         scanner.addIncludeFilter(typeFilter);
      }
   }
   
   // 判断是否有指定要排除Spring扫描的类
   for (AnnotationAttributes filter : componentScan.getAnnotationArray("excludeFilters")) {
      for (TypeFilter typeFilter : typeFiltersFor(filter)) {
         scanner.addExcludeFilter(typeFilter);
      }
   }
   // 是否延迟初始化
   boolean lazyInit = componentScan.getBoolean("lazyInit");
   if (lazyInit) {
      scanner.getBeanDefinitionDefaults().setLazyInit(true);
   }

   // 扫描的指定包名下的所有的Class类
   Set<String> basePackages = new LinkedHashSet<String>();
   String[] basePackagesArray = componentScan.getStringArray("basePackages");
   for (String pkg : basePackagesArray) {
      String[] tokenized = StringUtils.tokenizeToStringArray(this.environment.resolvePlaceholders(pkg),
            ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
      basePackages.addAll(Arrays.asList(tokenized));
   }
   for (Class<?> clazz : componentScan.getClassArray("basePackageClasses")) {
      basePackages.add(ClassUtils.getPackageName(clazz));
   }
   if (basePackages.isEmpty()) {
      basePackages.add(ClassUtils.getPackageName(declaringClass));
   }

   // 定义类
   // 卧槽，这个好像JS传入函数的方法-- 要学习一下
   scanner.addExcludeFilter(new AbstractTypeHierarchyTraversingFilter(false, false) {
      @Override
      protected boolean matchClassName(String className) {
         return declaringClass.equals(className);
      }
   });
   
   // 去做真正的扫描操作
   return scanner.doScan(StringUtils.toStringArray(basePackages));
}
```



**2.1.1.2  ClassPathBeanDefinitionScanner**

进行扫描获取到最后的BeanDefinition的集合

```
protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
   Assert.notEmpty(basePackages, "At least one base package must be specified");
   Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
   for (String basePackage : basePackages) {
      Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
      for (BeanDefinition candidate : candidates) {
         ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
         candidate.setScope(scopeMetadata.getScopeName());
         String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);
         if (candidate instanceof AbstractBeanDefinition) {
            postProcessBeanDefinition((AbstractBeanDefinition) candidate, beanName);
         }
         if (candidate instanceof AnnotatedBeanDefinition) {
            AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
         }
         if (checkCandidate(beanName, candidate)) {
            BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
            definitionHolder =
                  AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
            beanDefinitions.add(definitionHolder);
            registerBeanDefinition(definitionHolder, this.registry);
         }
      }
   }
   return beanDefinitions;
}
```
**2.1.1.3 ScopedProxyUtils**

如果检测到当前的Bean存在，需要进行代理的情况，则为其原始的BeanDefinition创建一个新的BeanDefinitionHolder以封装原始BeanDefinition和其代理类。

```
public static BeanDefinitionHolder createScopedProxy(BeanDefinitionHolder definition,
      BeanDefinitionRegistry registry, boolean proxyTargetClass) {

   String originalBeanName = definition.getBeanName();
   BeanDefinition targetDefinition = definition.getBeanDefinition();
   String targetBeanName = getTargetBeanName(originalBeanName);

   // Create a scoped proxy definition for the original bean name,
   // "hiding" the target bean in an internal target definition.
   RootBeanDefinition proxyDefinition = new RootBeanDefinition(ScopedProxyFactoryBean.class);
   proxyDefinition.setDecoratedDefinition(new BeanDefinitionHolder(targetDefinition, targetBeanName));
   proxyDefinition.setOriginatingBeanDefinition(targetDefinition);
   proxyDefinition.setSource(definition.getSource());
   proxyDefinition.setRole(targetDefinition.getRole());

   proxyDefinition.getPropertyValues().add("targetBeanName", targetBeanName);
   if (proxyTargetClass) {
      targetDefinition.setAttribute(AutoProxyUtils.PRESERVE_TARGET_CLASS_ATTRIBUTE, Boolean.TRUE);
      // ScopedProxyFactoryBean's "proxyTargetClass" default is TRUE, so we don't need to set it explicitly here.
   }
   else {
      proxyDefinition.getPropertyValues().add("proxyTargetClass", Boolean.FALSE);
   }

   // Copy autowire settings from original bean definition.
   proxyDefinition.setAutowireCandidate(targetDefinition.isAutowireCandidate());
   proxyDefinition.setPrimary(targetDefinition.isPrimary());
   if (targetDefinition instanceof AbstractBeanDefinition) {
      proxyDefinition.copyQualifiersFrom((AbstractBeanDefinition) targetDefinition);
   }

   // The target bean should be ignored in favor of the scoped proxy.
   targetDefinition.setAutowireCandidate(false);
   targetDefinition.setPrimary(false);

   // Register the target bean as separate bean in the factory.
   registry.registerBeanDefinition(targetBeanName, targetDefinition);

   // Return the scoped proxy definition as primary bean definition
   // (potentially an inner bean).
   return new BeanDefinitionHolder(proxyDefinition, originalBeanName, definition.getAliases());
}
```









###  我们看下调用链

![](http://oimj9bzzz.bkt.clouddn.com/18-8-17/73104706.jpg)

由上图我们很容易可以看到当我们去初始化我们的BeanFactoryPostProcessor的时候，这时候，我们会调用到我们的ConfigurationClassParser来解析我们整个包结构中的注解的读取。