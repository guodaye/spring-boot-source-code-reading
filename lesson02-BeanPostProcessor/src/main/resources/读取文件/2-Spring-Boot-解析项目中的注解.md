### 一、ConfigurationClassPostProcessor

#### 1.1 继承关系

![](http://oimj9bzzz.bkt.clouddn.com/18-8-17/13249075.jpg)

我们通过上述的继承关系，可以看出来该类是一个`BeanDefinitionRegistryPostProcessor`。因此当程序启动的时候，会去调用当前类的`postProcessBeanDefinitionRegistry`方法。于是在这个方法中她便进行整个项目中`BeanDefinition`的扫描。



#### 1.2 调用链是大爷

![](http://oimj9bzzz.bkt.clouddn.com/18-8-17/28321412.jpg)

![](http://oimj9bzzz.bkt.clouddn.com/18-8-17/87437646.jpg)

通过上述的两个图，我们可以很明显的看出整个调用链的流程，我们很容易证明我们上述的结论是正确的。

```
public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
   List<BeanDefinitionHolder> configCandidates = new ArrayList<BeanDefinitionHolder>();
   String[] candidateNames = registry.getBeanDefinitionNames();
   
   // 使用Spring-Boot默认的BeanDefinitionRegistry的时候，这时候candidateNames只有8个
   // 其中最重要的一个是我们main函数所在的实体类，我们的扫描也是通过这个实体类开始的。
   for (String beanName : candidateNames) {
      BeanDefinition beanDef = registry.getBeanDefinition(beanName);
      
      // 这里是判断一个类已经完全处理完毕？
      // 2018-08-17 存疑，如何模拟该值为true
      if (ConfigurationClassUtils.isFullConfigurationClass(beanDef) ||
            ConfigurationClassUtils.isLiteConfigurationClass(beanDef)) {
         if (logger.isDebugEnabled()) {
            logger.debug("Bean definition has already been processed as a configuration class: " + beanDef);
         }
      }
      
      // 获取当前的BeanDefinition是没有处理过的@Configuration类
      // @SpringBootApplication标注的类在慈湖会为true
      // 然后将其封装为一个BeanDefinitionHolder对象
      else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
         configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
      }
   }
   
   // 如果没有发现用@Configuration进行标注的类，则可以直接返回
   if (configCandidates.isEmpty()) {
      return;
   }

   // 排序这些待处理的BeanDefinitionHolder的先后顺序
   Collections.sort(configCandidates, new Comparator<BeanDefinitionHolder>() {
      @Override
      public int compare(BeanDefinitionHolder bd1, BeanDefinitionHolder bd2) {
         int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
         int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
         return (i1 < i2) ? -1 : (i1 > i2) ? 1 : 0;
      }
   });
     
   // 判断上下文中是否提供自定义的BeanName的生成器，如果开发者提供了该组件，则使用开发者的
   // BeanName生成器
   SingletonBeanRegistry sbr = null;
   if (registry instanceof SingletonBeanRegistry) {
      sbr = (SingletonBeanRegistry) registry;
      if (!this.localBeanNameGeneratorSet && sbr.containsSingleton(CONFIGURATION_BEAN_NAME_GENERATOR)) {
         BeanNameGenerator generator = (BeanNameGenerator) sbr.getSingleton(CONFIGURATION_BEAN_NAME_GENERATOR);
         this.componentScanBeanNameGenerator = generator;
         this.importBeanNameGenerator = generator;
      }
   }

   // Parse each @Configuration class
   // 解析所有的使用@Configuration注解的Java类
   ConfigurationClassParser parser = new ConfigurationClassParser(
         this.metadataReaderFactory, this.problemReporter, this.environment,
         this.resourceLoader, this.componentScanBeanNameGenerator, registry);

   Set<BeanDefinitionHolder> candidates = new LinkedHashSet<BeanDefinitionHolder>(configCandidates);
   Set<ConfigurationClass> alreadyParsed = new HashSet<ConfigurationClass>(configCandidates.size());
   do {
      // 此处进行解析整个项目中的@Configuration进行解析为BeanDefinitionHolder
      // 并添加到BeanDefinitionRegistry中
      parser.parse(candidates);
      
      parser.validate();

      Set<ConfigurationClass> configClasses = new LinkedHashSet<ConfigurationClass>(parser.getConfigurationClasses());
      configClasses.removeAll(alreadyParsed);

      // Read the model and create bean definitions based on its content
      if (this.reader == null) {
         this.reader = new ConfigurationClassBeanDefinitionReader(
               registry, this.sourceExtractor, this.resourceLoader, this.environment,
               this.importBeanNameGenerator, parser.getImportRegistry());
      }
      this.reader.loadBeanDefinitions(configClasses);
      alreadyParsed.addAll(configClasses);

      candidates.clear();
      if (registry.getBeanDefinitionCount() > candidateNames.length) {
         String[] newCandidateNames = registry.getBeanDefinitionNames();
         Set<String> oldCandidateNames = new HashSet<String>(Arrays.asList(candidateNames));
         Set<String> alreadyParsedClasses = new HashSet<String>();
         for (ConfigurationClass configurationClass : alreadyParsed) {
            alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());
         }
         for (String candidateName : newCandidateNames) {
            if (!oldCandidateNames.contains(candidateName)) {
               BeanDefinition bd = registry.getBeanDefinition(candidateName);
               if (ConfigurationClassUtils.checkConfigurationClassCandidate(bd, this.metadataReaderFactory) &&
                     !alreadyParsedClasses.contains(bd.getBeanClassName())) {
                  candidates.add(new BeanDefinitionHolder(bd, candidateName));
               }
            }
         }
         candidateNames = newCandidateNames;
      }
   }
   while (!candidates.isEmpty());

   // Register the ImportRegistry as a bean in order to support ImportAware @Configuration classes
   if (sbr != null) {
      if (!sbr.containsSingleton(IMPORT_REGISTRY_BEAN_NAME)) {
         sbr.registerSingleton(IMPORT_REGISTRY_BEAN_NAME, parser.getImportRegistry());
      }
   }

   if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory) {
      ((CachingMetadataReaderFactory) this.metadataReaderFactory).clearCache();
   }
}
```



###  二、ConfigurationClassParser

这个类是完成整个项目中所有的Bean的扫描的关键。



#### 2.1 从这里开始解析整个@SpringBootApplication的注解

```
public void parse(Set<BeanDefinitionHolder> configCandidates) {
   this.deferredImportSelectors = new LinkedList<DeferredImportSelectorHolder>();

   for (BeanDefinitionHolder holder : configCandidates) {
      BeanDefinition bd = holder.getBeanDefinition();
      try {
         // 下面的parse最后都会将传入的数据封装为一个ConfigurationClass类
         // 进行处理
         if (bd instanceof AnnotatedBeanDefinition) {
            // @SpringBootApplication注解的类，会在这里进行处理
            parse(((AnnotatedBeanDefinition) bd).getMetadata(), holder.getBeanName());
         }
         else if (bd instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) bd).hasBeanClass()) {
            parse(((AbstractBeanDefinition) bd).getBeanClass(), holder.getBeanName());
         }
         else {
            parse(bd.getBeanClassName(), holder.getBeanName());
         }
      }
      catch (BeanDefinitionStoreException ex) {
         throw ex;
      }
      catch (Throwable ex) {
         throw new BeanDefinitionStoreException(
               "Failed to parse configuration class [" + bd.getBeanClassName() + "]", ex);
      }
   }
   
   // 处理ImportSelector接口
   processDeferredImportSelectors();
}
```



#### 2.2 处理ConfigurationClass 

```
protected void processConfigurationClass(ConfigurationClass configClass) throws IOException {
   // 在此处会处理@Conditional系列的BeanDefinition的处理
   if (this.conditionEvaluator.shouldSkip(configClass.getMetadata(), ConfigurationPhase.PARSE_CONFIGURATION)) {
      return;
   }

   // 我们不知道这里是什么意思？？
   // 
   ConfigurationClass existingClass = this.configurationClasses.get(configClass);
   if (existingClass != null) {
      // 判断当前的ConfigurationClass是否已经导入
      if (configClass.isImported()) {
         if (existingClass.isImported()) {
         // 如果已经导入了就进行合并
            existingClass.mergeImportedBy(configClass);
         }
         // Otherwise ignore new imported config class; existing non-imported class overrides it.
         return;
      }
      else {
         // Explicit bean definition found, probably replacing an import.
         // Let's remove the old one and go with the new one.
         // 如果是发现一个显式定义的Bean：用xml定义的Bean等级高于使用注解定义的Bean？
         // 
         this.configurationClasses.remove(configClass);
         for (Iterator<ConfigurationClass> it = this.knownSuperclasses.values().iterator(); it.hasNext();) {
            if (configClass.equals(it.next())) {
               it.remove();
            }
         }
      }
   }

   // Recursively process the configuration class and its superclass hierarchy.
   // 循环处理Configuration类和它的子类
   SourceClass sourceClass = asSourceClass(configClass);
   do {
      sourceClass = doProcessConfigurationClass(configClass, sourceClass);
   }
   while (sourceClass != null);

   this.configurationClasses.put(configClass, configClass);
}
```



### 2.3 循环遍历

```
/**
 * 通过从类中读取注解，成员属性，方法来处理和构建成一个完整的ConfigurationClass
 * 这个方法可以被调用多次，一旦发现和它相关的类。
 * @param configClass 一个等待被构建的配置类
 * @param 一个类所在的文件
 * @return 返回父类，如果没有父类就返回null
 */
protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass)
      throws IOException {

   // Recursively process any member (nested) classes first
   processMemberClasses(configClass, sourceClass);

   // 处理@PropertySource注解
   for (AnnotationAttributes propertySource : AnnotationConfigUtils.attributesForRepeatable(
         sourceClass.getMetadata(), PropertySources.class,PropertySource.class)) {
      if (this.environment instanceof ConfigurableEnvironment) {
         processPropertySource(propertySource);
      }
      else {
         logger.warn("Ignoring @PropertySource annotation on [" + sourceClass.getMetadata().getClassName() +
               "]. Reason: Environment must implement ConfigurableEnvironment");
      }
   }

   // 处理@ComponentScan注解
   // Process any  annotations
   Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
         sourceClass.getMetadata(), ComponentScans.class, ComponentScan.class);
   // 获取的@ComponentScan属性不为空，且注解中没有使用@Conditional系列的注解
   // ConfigurationPhase.REGISTER_BEAN 如果一个类使用@Conditional就会被标注为该值
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

   // Process any @Import annotations
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

   // Process individual @Bean methods
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