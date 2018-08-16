本文中主要讲述的是如果从spring的容器中如何去实例化一个Bean。

该入口中最底层的调用是AbstracBeanFactory下的getBean方法

```
	/**
	 * 返回一个实例，或者是单例或者是原型
	 * @param 需要获取的bean的名字
	 * @param 需要获取的bean的Class的类型
	 * @param 创建bean时需要的参数，如果创建bean时，显式指定了参数，则会调用带参数的构造函数
	 * @param 是否需要对bean的实例进行类型检查，实际上使用的是否不需要检查
	 * @return 返回bean实例
	 * @throws BeansException 如果bean没有获得则抛出该异常
	 */
protected <T> T doGetBean(
    final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly)
    throws BeansException {
  // 将beanName进行标准化
  // 如果这个beanName是一个FactoryBean，则它的name是的前缀是带&的
  // 因此在此处需要将&这个符号去掉
  final String beanName = transformedBeanName(name);
  Object bean;

  // Eagerly check singleton cache for manually registered singletons.
  // 为手动注册的单例首先进行检查单例缓存
  Object sharedInstance = getSingleton(beanName);
  if (sharedInstance != null && args == null) {
    if (logger.isDebugEnabled()) {
      if (isSingletonCurrentlyInCreation(beanName)) {
        logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
            "' that is not fully initialized yet - a consequence of a circular reference");
      }
      else {
        logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
      }
    }
    bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
  }

  else {
    // 如果我们已经创建了所需要的bean，但是我们没有获取到，
    // 我们可以猜想他大概是因为循环依赖引起的失败
    if (isPrototypeCurrentlyInCreation(beanName)) {
      throw new BeanCurrentlyInCreationException(beanName);
    }

    // Check if bean definition exists in this factory.
    BeanFactory parentBeanFactory = getParentBeanFactory();
    if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
      // Not found -> check parent.
      String nameToLookup = originalBeanName(name);
      if (args != null) {
        // Delegation to parent with explicit args.
        return (T) parentBeanFactory.getBean(nameToLookup, args);
      }
      else {
        // No args -> delegate to standard getBean method.
        return parentBeanFactory.getBean(nameToLookup, requiredType);
      }
    }

    if (!typeCheckOnly) {
      markBeanAsCreated(beanName);
    }

    try {
      final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
      checkMergedBeanDefinition(mbd, beanName, args);

      // Guarantee initialization of beans that the current bean depends on.
      String[] dependsOn = mbd.getDependsOn();
      if (dependsOn != null) {
        for (String dep : dependsOn) {
          if (isDependent(beanName, dep)) {
            throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
          }
          registerDependentBean(dep, beanName);
          getBean(dep);
        }
      }

      // Create bean instance.
      if (mbd.isSingleton()) {
        sharedInstance = getSingleton(beanName, new ObjectFactory<Object>() {
          @Override
          public Object getObject() throws BeansException {
            try {
              return createBean(beanName, mbd, args);
            }
            catch (BeansException ex) {
              // Explicitly remove instance from singleton cache: It might have been put there
              // eagerly by the creation process, to allow for circular reference resolution.
              // Also remove any beans that received a temporary reference to the bean.
              destroySingleton(beanName);
              throw ex;
            }
          }
        });
        bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
      }

      else if (mbd.isPrototype()) {
        // It's a prototype -> create a new instance.
        Object prototypeInstance = null;
        try {
          beforePrototypeCreation(beanName);
          prototypeInstance = createBean(beanName, mbd, args);
        }
        finally {
          afterPrototypeCreation(beanName);
        }
        bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
      }

      else {
        String scopeName = mbd.getScope();
        final Scope scope = this.scopes.get(scopeName);
        if (scope == null) {
          throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
        }
        try {
          Object scopedInstance = scope.get(beanName, new ObjectFactory<Object>() {
            @Override
            public Object getObject() throws BeansException {
              beforePrototypeCreation(beanName);
              try {
                return createBean(beanName, mbd, args);
              }
              finally {
                afterPrototypeCreation(beanName);
              }
            }
          });
          bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
        }
        catch (IllegalStateException ex) {
          throw new BeanCreationException(beanName,
              "Scope '" + scopeName + "' is not active for the current thread; consider " +
              "defining a scoped proxy for this bean if you intend to refer to it from a singleton",
              ex);
        }
      }
    }
    catch (BeansException ex) {
      cleanupAfterBeanCreationFailure(beanName);
      throw ex;
    }
  }

  // Check if required type matches the type of the actual bean instance.
  if (requiredType != null && bean != null && !requiredType.isInstance(bean)) {
    try {
      return getTypeConverter().convertIfNecessary(bean, requiredType);
    }
    catch (TypeMismatchException ex) {
      if (logger.isDebugEnabled()) {
        logger.debug("Failed to convert bean '" + name + "' to required type '" +
            ClassUtils.getQualifiedName(requiredType) + "'", ex);
      }
      throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
    }
  }
  return (T) bean;
}
```


这里是可以获取到一个



```
/**
 * 通过给定的bean的name获取到最原始的一个单例(raw bean)
 * 检测已经实例化的单例，同时也允许提前实例化(解决循环依赖的问题)
 * @param beanName 要寻找的bean的name
 * @param allowEarlyReference 是否允许bean进行提前实例化
 * @return 返回已经注册的单例实例，如果没有注册则返回null
 */
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
   Object singletonObject = this.singletonObjects.get(beanName);
   if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
      synchronized (this.singletonObjects) {
         singletonObject = this.earlySingletonObjects.get(beanName);
         if (singletonObject == null && allowEarlyReference) {
            ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
            if (singletonFactory != null) {
               singletonObject = singletonFactory.getObject();
               this.earlySingletonObjects.put(beanName, singletonObject);
               this.singletonFactories.remove(beanName);
            }
         }
      }
   }
   return (singletonObject != NULL_OBJECT ? singletonObject : null);
}
```