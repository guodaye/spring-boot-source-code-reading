### 一、 BeanUtils

#### 1. instantiateClass

根据传入的Class类型，调用目标Class的无参构造函数

```
public static <T> T instantiateClass(Class<T> clazz) throws BeanInstantiationException {
   Assert.notNull(clazz, "Class must not be null");
   if (clazz.isInterface()) {
      throw new BeanInstantiationException(clazz, "Specified class is an interface");
   }
   try {
      return instantiateClass(clazz.getDeclaredConstructor());
   }
   catch (NoSuchMethodException ex) {
      throw new BeanInstantiationException(clazz, "No default constructor found", ex);
   }
}
```



### 二、ComponentScan

- @Component
- @Configuration
- @Import
- @ImportResource
- @PropertySources
- @PropertySource
- @Bean

