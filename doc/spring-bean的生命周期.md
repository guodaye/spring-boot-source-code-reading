### 零、堆栈信息看入口

```
createBean:448, AbstractAutowireCapableBeanFactory (org.springframework.beans.factory.support)
getObject:306, AbstractBeanFactory$1 (org.springframework.beans.factory.support)
getSingleton:230, DefaultSingletonBeanRegistry (org.springframework.beans.factory.support)
doGetBean:302, AbstractBeanFactory (org.springframework.beans.factory.support)
getBean:197, AbstractBeanFactory (org.springframework.beans.factory.support)
```

我们从上面的堆栈信息很容易可以看出来，我们很容易可以看到整个spring初始化一个Bean的堆栈情况。



```java
// org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBean（）
// 会对方法做精简，去掉部分无关紧要的代码/注释/日志
@Override
protected Object createBean(String beanName, RootBeanDefinition mbd, Object[] args) throws BeanCreationException {
   // 首先获取BeanDefinition
   RootBeanDefinition mbdToUse = mbd;

   // 一、决定beanName所需要初始化的Class类型
   // 如果这个bean class 是一个动态的Bean Class，则需要克隆原来的bean class
   // 防止污染原来的BeanClass
   Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
   if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
      mbdToUse = new RootBeanDefinition(mbd);
      mbdToUse.setBeanClass(resolvedClass);
   }

   // 判断当前bean的及其父类有哪些同名的Bean是需要进行覆盖的
   try {
      mbdToUse.prepareMethodOverrides();
   }
   catch (BeanDefinitionValidationException ex) {
      throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
            beanName, "Validation of method overrides failed", ex);
   }

   try {
      // 如果该bean需要进行代理的包装，则给代理的后置处理器一个机会
      // 也就是说，所有需要用代理进行包装的Bean，都会在此处就结束程序进行返回了
      Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
      if (bean != null) {
         return bean;
      }
   }
   catch (Throwable ex) {
      throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
            "BeanPostProcessor before instantiation of bean failed", ex);
   }

   Object beanInstance = doCreateBean(beanName, mbdToUse, args);
   if (logger.isDebugEnabled()) {
      logger.debug("Finished creating instance of bean '" + beanName + "'");
   }
   return beanInstance;
}
```



### 一、 决定类型

```java
	protected Class<?> resolveBeanClass(final RootBeanDefinition mbd, String beanName, final Class<?>... typesToMatch)
			throws CannotLoadBeanClassException {
		try {
		    // 判断当前的beanClass是不是一个Class类
             // 是的话，可以直接返回这个Bean的Class
			if (mbd.hasBeanClass()) {
				return mbd.getBeanClass();
			}
			if (System.getSecurityManager() != null) {
				return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
					@Override
					public Class<?> run() throws Exception {
						return doResolveBeanClass(mbd, typesToMatch);
					}
				}, getAccessControlContext());
			}
			else {
                 // 获取bean的Class
				return doResolveBeanClass(mbd, typesToMatch);
			}
		}
		catch (Exception pae) {
            // 如果异常，则会在此处进行捕获并抛出
            // 删除掉原来的异常逻辑
		}
	}

```

```java
private Class<?> doResolveBeanClass(RootBeanDefinition mbd, Class<?>... typesToMatch)
      throws ClassNotFoundException {
   
   ClassLoader beanClassLoader = getBeanClassLoader();
   ClassLoader classLoaderToUse = beanClassLoader;
   // 首先判断typesToMatch是否为空
   // 如果不为空，则使用临时类加载器，并把这些typesToMatch添加到DecoratingClassLoader
   // 中，并且不允许对其进行装饰，也就是说其显示的只是raw Bean
   if (!ObjectUtils.isEmpty(typesToMatch)) {
      ClassLoader tempClassLoader = getTempClassLoader();
      if (tempClassLoader != null) {
         classLoaderToUse = tempClassLoader;
         if (tempClassLoader instanceof DecoratingClassLoader) {
            DecoratingClassLoader dcl = (DecoratingClassLoader) tempClassLoader;
            for (Class<?> typeToMatch : typesToMatch) {
               dcl.excludeClass(typeToMatch.getName());
            }
         }
      }
   }
   
   String className = mbd.getBeanClassName();
   if (className != null) {
      // 此处会注册一系列的SpEL语言解析器
      Object evaluated = evaluateBeanDefinitionString(className, mbd);
      if (!className.equals(evaluated)) {
         if (evaluated instanceof Class) {
            return (Class<?>) evaluated;
         }
         else if (evaluated instanceof String) {
            return ClassUtils.forName((String) evaluated, classLoaderToUse);
         }
         else {
            throw new IllegalStateException("Invalid class name expression result: " + evaluated);
         }
      }
      // When resolving against a temporary class loader, exit early in order
      // to avoid storing the resolved Class in the bean definition.
      // 解决，如果使用temporary的类加载器，为了避免在bean定义中存储了已经解析的class类
      // 需要进行提早退出
      if (classLoaderToUse != beanClassLoader) {
         return ClassUtils.forName(className, classLoaderToUse);
      }
   }
   // 获取Bean的Class
   return mbd.resolveBeanClass(beanClassLoader);
}
```



```java
// org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#doCreateBean
protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args)
      throws BeanCreationException {

   // 二、实例化一个bean
   // 根据一个beanname获取获取一个BeanWrapper
   // 通过BeanWrapper获取到一个bean和bean的class
   BeanWrapper instanceWrapper = null;
   if (mbd.isSingleton()) {
      instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
   }
   if (instanceWrapper == null) {
      instanceWrapper = createBeanInstance(beanName, mbd, args);
   }
   final Object bean = (instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null);
   Class<?> beanType = (instanceWrapper != null ? instanceWrapper.getWrappedClass() : null);
   mbd.resolvedTargetType = beanType;

   // 允许通过MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition()
   // 修改bean definition
   synchronized (mbd.postProcessingLock) {
      if (!mbd.postProcessed) {
         try {
            applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
         }
         catch (Throwable ex) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                  "Post-processing of merged bean definition failed", ex);
         }
         mbd.postProcessed = true;
      }
   }

   // spring通过提前暴露实例的引用来解决循环依赖
   // BeanFactoryAware这类的lifecycle的接口也可以触发提前实例化
   boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
         isSingletonCurrentlyInCreation(beanName));
   if (earlySingletonExposure) {
      if (logger.isDebugEnabled()) {
         logger.debug("Eagerly caching bean '" + beanName +
               "' to allow for resolving potential circular references");
      }
      addSingletonFactory(beanName, new ObjectFactory<Object>() {
         @Override
         public Object getObject() throws BeansException {
            // 提前获取引用对象，有就获取，没有拉到
            return getEarlyBeanReference(beanName, mbd, bean);
         }
      });
   }

   // 实例化得到一个raw bean
   Object exposedObject = bean;
   try {
      // 注入属性
      populateBean(beanName, mbd, instanceWrapper);
      if (exposedObject != null) {
         exposedObject = initializeBean(beanName, exposedObject, mbd);
      }
   }
   catch (Throwable ex) {
      if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
         throw (BeanCreationException) ex;
      }
      else {
         throw new BeanCreationException(
               mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
      }
   }

   if (earlySingletonExposure) {
      Object earlySingletonReference = getSingleton(beanName, false);
      if (earlySingletonReference != null) {
         if (exposedObject == bean) {
            exposedObject = earlySingletonReference;
         }
         else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
            String[] dependentBeans = getDependentBeans(beanName);
            Set<String> actualDependentBeans = new LinkedHashSet<String>(dependentBeans.length);
            for (String dependentBean : dependentBeans) {
               if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                  actualDependentBeans.add(dependentBean);
               }
            }
            if (!actualDependentBeans.isEmpty()) {
               throw new BeanCurrentlyInCreationException(beanName,
                     "Bean with name '" + beanName + "' has been injected into other beans [" +
                     StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
                     "] in its raw version as part of a circular reference, but has eventually been " +
                     "wrapped. This means that said other beans do not use the final version of the " +
                     "bean. This is often the result of over-eager type matching - consider using " +
                     "'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
            }
         }
      }
   }

   // Register bean as disposable.
   try {
      // 处理@PreDestory的方法的注册
      registerDisposableBeanIfNecessary(beanName, bean, mbd);
   }
   catch (BeanDefinitionValidationException ex) {
      throw new BeanCreationException(
            mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
   }

   return exposedObject;
}
```



```java
// 填充Bean
protected void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw) {
		PropertyValues pvs = mbd.getPropertyValues();

		if (bw == null) {
			if (!pvs.isEmpty()) {
				throw new BeanCreationException(
						mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
			}
			else {
				// Skip property population phase for null instance.
				return;
			}
		}

		boolean continueWithPropertyPopulation = true;
         // 给InstantiationAwareBeanPostProcessors一个执行的机会：在对bean设置属性前，进行
         // 修改bean的状态。
		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof InstantiationAwareBeanPostProcessor) {
					InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
					if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
						continueWithPropertyPopulation = false;
						break;
					}
				}
			}
		}
         // 不继续进行填充属性，直接返回
		if (!continueWithPropertyPopulation) {
			return;
		}

         // @Value @Autowired 根据类型或者是根据beanName进行注入
         // 默认是RootBeanDefinition.AUTOWIRE_NO
		if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME ||
				mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
			MutablePropertyValues newPvs = new MutablePropertyValues(pvs);

			// Add property values based on autowire by name if applicable.
			if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
				autowireByName(beanName, mbd, bw, newPvs);
			}

			// Add property values based on autowire by type if applicable.
			if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
				autowireByType(beanName, mbd, bw, newPvs);
			}
			pvs = newPvs;
		}

		boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
		boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);

		if (hasInstAwareBpps || needsDepCheck) {
             // 获取bean的依赖属性
			PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
			if (hasInstAwareBpps) {
                 // InstantiationAwareBeanPostProcessor#postProcessPropertyValues
                 // 在此处会处理RootBeanDefinition.AUTOWIRE_NO的依赖注入
				for (BeanPostProcessor bp : getBeanPostProcessors()) {
					if (bp instanceof InstantiationAwareBeanPostProcessor) {
						InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
						pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
						if (pvs == null) {
							return;
						}
					}
				}
			}
             // 是否需要检查当前的bean中所有的依赖
             // 以及依赖的依赖是否全部都设置好了，没有设置好会报错
			if (needsDepCheck) {
				checkDependencies(beanName, mbd, filteredPds, pvs);
			}
		}
         // SpEL属性处理
		applyPropertyValues(beanName, mbd, bw, pvs);
	}
```



```java
// 属性后置
protected Object initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd) {
   // BeanNameAware  BeanClassLoaderAware BeanFactoryAware注入相应的依赖
   if (System.getSecurityManager() != null) {
      AccessController.doPrivileged(new PrivilegedAction<Object>() {
         @Override
         public Object run() {
            invokeAwareMethods(beanName, bean);
            return null;
         }
      }, getAccessControlContext());
   }
   else {
      invokeAwareMethods(beanName, bean);
   }

   Object wrappedBean = bean;
   if (mbd == null || !mbd.isSynthetic()) {
      // 执行postProcessBeforeInitialization方法
      // @PostConstruct
      wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
   }

   try {
      // 执行InitializeBean中的afterPropertiesSet方法
      invokeInitMethods(beanName, wrappedBean, mbd);
   }
   catch (Throwable ex) {
      throw new BeanCreationException(
            (mbd != null ? mbd.getResourceDescription() : null),
            beanName, "Invocation of init method failed", ex);
   }

   if (mbd == null || !mbd.isSynthetic()) {
      // postProcessAfterInitialization
      wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
   }
   return wrappedBean;
}
```



### 二、 实例化

### 三、初始化钱的前置处理

### 四、初始化

### 五、属性后置处理

### 六、初始化后置处理

### 七、销毁



### 六、代理bean最屌

```java
protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
   Object bean = null;
   // 判断当前的bean是否已经进行过前置初始化处理
   // 是的话，直接返回bean
   // 否则的话，进行实例化
   if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
      // Make sure bean class is actually resolved at this point.
      // 确保bean class 在这个地方被正确的处理
      // mbd.isSynthetic() 是否是合成的，默认为false
      // hasInstantiationAwareBeanPostProcessors 是否拥有初始化前置Aware处理器
      if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
         // 获取目标的bean class
         Class<?> targetType = determineTargetType(beanName, mbd);
         if (targetType != null) {
            bean = applyBeanPostProcessorsBeforeInstantiation(targetType, beanName);
            if (bean != null) {
               bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
            }
         }
      }
      mbd.beforeInstantiationResolved = (bean != null);
   }
   // 此处可以直接返回代理bean
   return bean;
}
```

