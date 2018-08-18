### 一、BeanWrapper

```
package org.springframework.beans;

import java.beans.PropertyDescriptor;

/**
 * spring 中最低层次的JavaBean的最基本描述接口
 *
 *  一般不直接使用这个接口，而是通过 {@link org.springframework.beans.factory.BeanFactory}
 *  或者是通过{@link org.springframework.validation.DataBinder}来使用
 * 
 * 提供对标准JavaBean的分析和修改操作：
 * 1. 为独立或者是分散的属性的进行set或者是get操作
 * 2. 获取get的属性描述
 * 3. 查询对于属性是否具备读写能力
 * 
 * 这个接口同样也支持对无限制的内部子类的属性嵌套的读写值的能力。
 *
 * 一个BeanWrapper的extractOldValueForEditor默认值是false
 * 以避免执行执行get操作时带来的副作用。
 * 如果需要向外暴露自定义编辑值的能力，则可以将其设置为true
 *
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13 April 2001
 * @see PropertyAccessor
 * @see PropertyEditorRegistry
 * @see PropertyAccessorFactory#forBeanPropertyAccess
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.validation.BeanPropertyBindingResult
 * @see org.springframework.validation.DataBinder#initBeanPropertyAccess()
 */
public interface BeanWrapper extends ConfigurablePropertyAccessor {

   /**
    * 为自增长的数组或者是集合设置一个最大值
    * 默认值为Integer.MAX_VALUE
    * @since 4.1
    */
   void setAutoGrowCollectionLimit(int autoGrowCollectionLimit);

   /**
    * 获取数组或者是集合的最大值
    * @since 4.1
    */
   int getAutoGrowCollectionLimit();

   /**
    * 返回bean被包装后的实例
    */
   Object getWrappedInstance();

   /**
    * 被包装的bean实例的Class类型
    */
   Class<?> getWrappedClass();

   /**
    * 获取被包装的bean中的属性描述
    */
   PropertyDescriptor[] getPropertyDescriptors();

   /**
    * 根据给定的属性名字获取bean中的属性描述
    * 如果给定的属性名字不存在，则抛出异常
    */
   PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException;

}
```



### 二、BeanWrapper的UML图

![](http://oimj9bzzz.bkt.clouddn.com/18-8-15/5972245.jpg)



|      |      |
| ---- | ---- |
|      |      |
|      |      |
|      |      |

