### 一、ComponentScanAnnotationParser

```java
	public Set<BeanDefinitionHolder> parse(AnnotationAttributes componentScan, final String declaringClass) {
		Assert.state(this.environment != null, "Environment must not be null");
		Assert.state(this.resourceLoader != null, "ResourceLoader must not be null");

        // 配置类路径下的扫描器
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(this.registry,
				componentScan.getBoolean("useDefaultFilters"), this.environment, this.resourceLoader);

		Class<? extends BeanNameGenerator> generatorClass = componentScan.getClass("nameGenerator");
		boolean useInheritedGenerator = (BeanNameGenerator.class == generatorClass);
		scanner.setBeanNameGenerator(useInheritedGenerator ? this.beanNameGenerator :
				BeanUtils.instantiateClass(generatorClass));

		ScopedProxyMode scopedProxyMode = componentScan.getEnum("scopedProxy");
		if (scopedProxyMode != ScopedProxyMode.DEFAULT) {
			scanner.setScopedProxyMode(scopedProxyMode);
		}
		else {
			Class<? extends ScopeMetadataResolver> resolverClass = componentScan.getClass("scopeResolver");
			scanner.setScopeMetadataResolver(BeanUtils.instantiateClass(resolverClass));
		}

		scanner.setResourcePattern(componentScan.getString("resourcePattern"));

		for (AnnotationAttributes filter : componentScan.getAnnotationArray("includeFilters")) {
			for (TypeFilter typeFilter : typeFiltersFor(filter)) {
				scanner.addIncludeFilter(typeFilter);
			}
		}
		for (AnnotationAttributes filter : componentScan.getAnnotationArray("excludeFilters")) {
			for (TypeFilter typeFilter : typeFiltersFor(filter)) {
				scanner.addExcludeFilter(typeFilter);
			}
		}

		boolean lazyInit = componentScan.getBoolean("lazyInit");
		if (lazyInit) {
			scanner.getBeanDefinitionDefaults().setLazyInit(true);
		}

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

		scanner.addExcludeFilter(new AbstractTypeHierarchyTraversingFilter(false, false) {
			@Override
			protected boolean matchClassName(String className) {
				return declaringClass.equals(className);
			}
		});
        // 进行真正的扫描操作
		return scanner.doScan(StringUtils.toStringArray(basePackages));
	}
```

上述的方法主要完成两个操作

1. 配置`ClassPathBeanDefinitionScanner`扫描器的属性
2. 调用`ClassPathBeanDefinitionScanner`的`doScan`进行扫描

### 二、ClassPathBeanDefinitionScanner

```java
protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
   Assert.notEmpty(basePackages, "At least one base package must be specified");
   Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<BeanDefinitionHolder>();
   for (String basePackage : basePackages) {
      // 获取所有的BeanDefinition
      Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
       
      for (BeanDefinition candidate : candidates) {
         // 设置ScopeMetadata
         ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
         // 设置Scope
         candidate.setScope(scopeMetadata.getScopeName());
         // 获取BeanName
         String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);
         // 注册bean
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
``` java
/**
 * 扫描指定包及其子包下所有的待检测是否存在注解的包名
 * @param basePackage 指定要检查注解的包名
 * @return 可以进行自动注入的BeanDefinition的集合
 */
public Set<BeanDefinition> findCandidateComponents(String basePackage) {
   Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
   try {
      // 转化为这种形式：classpath*:com/github/guoyaohui/**/*.class
      String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
            resolveBasePackage(basePackage) + '/' + this.resourcePattern;
      // 获取该包名下所有的class文件
      Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
      // 提前获取莫非是为了提供性能，毕竟不停的调用是有性能损耗的？
      boolean traceEnabled = logger.isTraceEnabled();
      boolean debugEnabled = logger.isDebugEnabled();
      for (Resource resource : resources) {
         if (traceEnabled) {
            logger.trace("Scanning " + resource);
         }
         if (resource.isReadable()) {
            try {
               // 根据文件获取元数据读取器
               MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
               if (isCandidateComponent(metadataReader)) {
                  ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                  sbd.setResource(resource);
                  sbd.setSource(resource);
                  // 判断当前文件是否需要跳过
                  // 会在这里进行@Conditional的计算，如果不符合@Conditional的计算的.class文件就无法添加到待处理的Bean的集合中
                  // 同时，如果ScannedGenericBeanDefinition不是待处理的@Componet
                  if (isCandidateComponent(sbd)) {
                     if (debugEnabled) {
                        logger.debug("Identified candidate component class: " + resource);
                     }
                     candidates.add(sbd);
                  }
                  else {
                     if (debugEnabled) {
                        logger.debug("Ignored because not a concrete top-level class: " + resource);
                     }
                  }
               }
               else {
                  if (traceEnabled) {
                     logger.trace("Ignored because not matching any filter: " + resource);
                  }
               }
            }
            catch (Throwable ex) {
               throw new BeanDefinitionStoreException(
                     "Failed to read candidate component class: " + resource, ex);
            }
         }
         else {
            if (traceEnabled) {
               logger.trace("Ignored because not readable: " + resource);
            }
         }
      }
   }
   catch (IOException ex) {
      throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
   }
   return candidates;
}
```



### 二、ClassPathScanningCandidateComponentProvider

#### 2.1 成为一个Component的两步骤走

##### 2.1.1 文件层面的检验

```java
/**
 * 判断给定的Class不在exclude filter且至少匹配一个include filter
 * @param metadataReader 该类的ASM的类加载器(一个xxx.class --> 一个Class类 --> 一个MetadataReader)
 * @return 该文件/Class/MetadataReader是否有资格成为一个Component
 */
protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
	for (TypeFilter tf : this.excludeFilters) {
		if (tf.match(metadataReader, this.metadataReaderFactory)) {
			return false;
		}
	}
    // includeFilters可能不止一个@Component
	for (TypeFilter tf : this.includeFilters) {
		if (tf.match(metadataReader, this.metadataReaderFactory)) {
			return isConditionMatch(metadataReader);
		}
	}
	return false;
}
```

```java
/**
 * 判断一个待选的component是否具备任何 @Conditional系列的注解
 * @param metadataReader 该类的ASM的类加载器(一个xxx.class --> 一个Class类 --> 一个MetadataReader)
 * @return 该文件/Class/MetadataReader是否有资格成为一个Component
 */
private boolean isConditionMatch(MetadataReader metadataReader) {
   if (this.conditionEvaluator == null) {
      this.conditionEvaluator = new ConditionEvaluator(getRegistry(), getEnvironment(), getResourceLoader());
   }
   return !this.conditionEvaluator.shouldSkip(metadataReader.getAnnotationMetadata());
}
```

##### 2.1.2 注解层面是否正确

```java
/**
 * Determine whether the given bean definition qualifies as candidate.
 * 检查
 * <p>The default implementation checks whether the class is not an interface
 * and not dependent on an enclosing class.
 * <p>Can be overridden in subclasses.
 * @param beanDefinition the bean definition to check
 * @return whether the bean definition qualifies as a candidate component
 */
protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
   AnnotationMetadata metadata = beanDefinition.getMetadata();
   
    // metadata.isIndependent()
   return (metadata.isIndependent() && (metadata.isConcrete() ||
         (metadata.isAbstract() && metadata.hasAnnotatedMethods(Lookup.class.getName()))));
}
```

**根据2.1.1 我们肯定会有疑问，怎么问 `this.includeFilters`到底是怎么来的呢？？？**

我们可以在启动`Spring-Boot`的时候，我们可以看到这么一个`registerDefaultFilters`方法。

#### 2.1. 添加this.includeFilters

```java
protected void registerDefaultFilters() {
   // 注册@Component
   this.includeFilters.add(new AnnotationTypeFilter(Component.class));
   ClassLoader cl = ClassPathScanningCandidateComponentProvider.class.getClassLoader();
   try {
      // 注册@ManagedBean
      this.includeFilters.add(new AnnotationTypeFilter(
            ((Class<? extends Annotation>) ClassUtils.forName("javax.annotation.ManagedBean", cl)), false));
      logger.debug("JSR-250 'javax.annotation.ManagedBean' found and supported for component scanning");
   }
   catch (ClassNotFoundException ex) {
      // JSR-250 1.1 API (as included in Java EE 6) not available - simply skip.
   }
   try {
       // 注册@Named
      this.includeFilters.add(new AnnotationTypeFilter(
            ((Class<? extends Annotation>) ClassUtils.forName("javax.inject.Named", cl)), false));
      logger.debug("JSR-330 'javax.inject.Named' annotation found and supported for component scanning");
   }
   catch (ClassNotFoundException ex) {
      // JSR-330 API not available - simply skip.
   }
}
```

当Spring-Boot扫描注解的话，必须要找`this.includeFilters`中具有的注解的类，也就是说如果我们自定义注解，可以采用在这里进行添加。

### 2.2 AnnotationScopeMetadataResolver

```java
@Override
public ScopeMetadata resolveScopeMetadata(BeanDefinition definition) {
   ScopeMetadata metadata = new ScopeMetadata();
   if (definition instanceof AnnotatedBeanDefinition) {
      // 如果是AnnotatedBeanDefinition
      AnnotatedBeanDefinition annDef = (AnnotatedBeanDefinition) definition;
      // 判断注解的属性中是否有@Scope这个注解
      AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(annDef.getMetadata(), this.scopeAnnotationType);
      // 获取@Scope注解中的属性
      if (attributes != null) {
         // @Scope中value作为scopeName
         metadata.setScopeName(attributes.getString("value"));
         // @Scope中的value作为设置代理模式
         ScopedProxyMode proxyMode = attributes.getEnum("proxyMode");
         // 如果没有设置代理模式或者是ScopedProxyMode.DEFAULT(其实两者是一样的)
         if (proxyMode == null || proxyMode == ScopedProxyMode.DEFAULT) {
            // 如果defaultProxyMode，则默认为ScopedProxyMode.NO
            proxyMode = this.defaultProxyMode;
         }
         // 设置代理模式
         metadata.setScopedProxyMode(proxyMode);
      }
   }
   return metadata;
}
```

### 2.3 AnnotationBeanNameGenerator

#### 2.3.1 获取BeanName

```java
@Override
public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
   if (definition instanceof AnnotatedBeanDefinition) {
      // 如果BeanDefinition是AnnotatedBeanDefinition
      String beanName = determineBeanNameFromAnnotation((AnnotatedBeanDefinition) definition);
      if (StringUtils.hasText(beanName)) {
         // Explicit bean name found.
         return beanName;
      }
   }
   // Fallback: generate a unique default bean name.
   return buildDefaultBeanName(definition, registry);
}
```

#### 2.3.2 获取注解上的value的BeanName

```java
/**
 * Derive a bean name from one of the annotations on the class.
 * @param annotatedDef the annotation-aware bean definition
 * @return the bean name, or {@code null} if none is found
 */
protected String determineBeanNameFromAnnotation(AnnotatedBeanDefinition annotatedDef) {
   // 获取AnnotatedBeanDefinition的注解元数据
   AnnotationMetadata amd = annotatedDef.getMetadata();
   // 获取这些注解元数据的集合
   Set<String> types = amd.getAnnotationTypes();
   String beanName = null;
   for (String type : types) {
      // 获取每一个注解的属性
      AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(amd, type);
      if (isStereotypeWithNameValue(type, amd.getMetaAnnotationTypes(type), attributes)) {
         Object value = attributes.get("value");
         if (value instanceof String) {
            String strVal = (String) value;
            if (StringUtils.hasLength(strVal)) {
               if (beanName != null && !strVal.equals(beanName)) {
                  throw new IllegalStateException("Stereotype annotations suggest inconsistent " +
                        "component names: '" + beanName + "' versus '" + strVal + "'");
               }
               beanName = strVal;
            }
         }
      }
   }
   return beanName;
}
```

```java
/**
* 
* Check whether the given annotation is a stereotype that is allowed
* to suggest a component name through its annotation {@code value()}.
* @param annotationType 等待检查的注解的String的类型
* @param metaAnnotationTypes 判断当前注解的注解的类型，包括了注解的所有的注解的注解
* @param 当前注解中的属性的map
* @return 是否可以使用
*/
protected boolean isStereotypeWithNameValue(String annotationType,
      Set<String> metaAnnotationTypes, Map<String, Object> attributes) {

   // annotationType.equals(COMPONENT_ANNOTATION_CLASSNAME) 注解类型为@Component
   // metaAnnotationTypes != null 注解的注解不为空
   // metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME)注解的注解中包含了@Component
   // annotationType.equals("javax.annotation.ManagedBean")注解类型为ManagedBean
   // annotationType.equals("javax.inject.Named")注解类型为Named
   boolean isStereotype = annotationType.equals(COMPONENT_ANNOTATION_CLASSNAME) ||
         (metaAnnotationTypes != null && metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME)) ||
         annotationType.equals("javax.annotation.ManagedBean") ||
         annotationType.equals("javax.inject.Named");
         
   // 满足以上的条件，且满足注解的属性中有value的属性
   return (isStereotype && attributes != null && attributes.containsKey("value"));
}
```

我觉得上面说的很绕，给你们看下什么是一图知晓。

![](http://oimj9bzzz.bkt.clouddn.com/18-8-17/73574308.jpg)

我们举例，比如说：

**1. org.springframework.boot.autoconfigure.SpringBootApplication**  

**2. 它拥有的所有的注解有：**

![](http://oimj9bzzz.bkt.clouddn.com/18-8-17/10122743.jpg)

**3. 它的属性是**

![](http://oimj9bzzz.bkt.clouddn.com/18-8-17/30028229.jpg)

有没有很浅显易懂。

#### 2.3.3 首字母小写的类型为beanname

```java
protected String buildDefaultBeanName(BeanDefinition definition) {
   // 获取bean的Class的短名字
   String shortClassName = ClassUtils.getShortName(definition.getBeanClassName());
   // 将首字母小写，其余字母不变作为Beanname
   return Introspector.decapitalize(shortClassName);
}
```



### 2.4 ClassPathBeanDefinitionScanner

#### 2.4.1 将默认的beanDefinitionDefaults的值设置给当前获取的beanDefinition

```
protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName) {
   beanDefinition.applyDefaults(this.beanDefinitionDefaults);
   if (this.autowireCandidatePatterns != null) {
      beanDefinition.setAutowireCandidate(PatternMatchUtils.simpleMatch(this.autowireCandidatePatterns, beanName));
   }
}
```



### 2.5 AnnotationConfigUtils

#### 2.5.1设置常见的注解

```
public static void processCommonDefinitionAnnotations(AnnotatedBeanDefinition abd) {
   processCommonDefinitionAnnotations(abd, abd.getMetadata());
}
```

#### 2.5.2 设置常见的注解

```java
static void processCommonDefinitionAnnotations(AnnotatedBeanDefinition abd, AnnotatedTypeMetadata metadata) {
   // 设置@Lazy
   if (metadata.isAnnotated(Lazy.class.getName())) {
      abd.setLazyInit(attributesFor(metadata, Lazy.class).getBoolean("value"));
   }
   else if (abd.getMetadata() != metadata && abd.getMetadata().isAnnotated(Lazy.class.getName())) {
      abd.setLazyInit(attributesFor(abd.getMetadata(), Lazy.class).getBoolean("value"));
   }
   
   // 设置@Primary
   if (metadata.isAnnotated(Primary.class.getName())) {
      abd.setPrimary(true);
   }
   // 设置@DepondsOn
   if (metadata.isAnnotated(DependsOn.class.getName())) {
      abd.setDependsOn(attributesFor(metadata, DependsOn.class).getStringArray("value"));
   }
   
   if (abd instanceof AbstractBeanDefinition) {
      AbstractBeanDefinition absBd = (AbstractBeanDefinition) abd;
      // 设置@Role
      if (metadata.isAnnotated(Role.class.getName())) {
         absBd.setRole(attributesFor(metadata, Role.class).getNumber("value").intValue());
      }
      // 设置@Description
      if (metadata.isAnnotated(Description.class.getName())) {
         absBd.setDescription(attributesFor(metadata, Description.class).getString("value"));
      }
   }
}
```

### 2.6 检查BeanName是否重复

```java
/**
 * Check the given candidate's bean name, determining whether the corresponding
 * bean definition needs to be registered or conflicts with an existing definition.
 * @param beanName the suggested name for the bean
 * @param beanDefinition the corresponding bean definition
 * @return 如果beanname没有重复返回true，重复了返回false
 * @throws ConflictingBeanDefinitionException if an existing, incompatible
 * bean definition has been found for the specified name
 */
protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
   // beanName是否存在于beanDefinitionMap中
   // 不存在返回false，存在返回true
   if (!this.registry.containsBeanDefinition(beanName)) {
      return true;
   }
   // 获取beanDefinitionMap中的BeanDefinition
   BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
   // 获取旧的BeanDefinition
   BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
   if (originatingDef != null) {
      existingDef = originatingDef;
   }
   
   if (isCompatible(beanDefinition, existingDef)) {
      return false;
   }
   throw new ConflictingBeanDefinitionException("Annotation-specified bean name '" + beanName +
         "' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, " +
         "non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
}
```

如果出现了两个BeanName一样的需要进行判断是不是兼容的，如果不是会导致程序抛出异常。

```java
protected boolean isCompatible(BeanDefinition newDefinition, BeanDefinition existingDefinition) {
   // 注：如果是使用Spring-Boot全部注解的方式进行构造的BeanDefinition都是ScannedGenericBeanDefinition
   // ！(existingDefinition instanceof ScannedGenericBeanDefinition) 如果之前是ScannedGenericBeanDefinition的实例，覆盖
   // 扫描同一个文件两次
   // 扫描同一个class两次
   return (!(existingDefinition instanceof ScannedGenericBeanDefinition) ||  // explicitly registered overriding bean
         newDefinition.getSource().equals(existingDefinition.getSource()) ||  // scanned same file twice
         newDefinition.equals(existingDefinition));  // scanned equivalent class twice
}
```