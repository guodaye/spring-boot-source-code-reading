

```
static Set<AnnotationAttributes> attributesForRepeatable(AnnotationMetadata metadata,
      String containerClassName, String annotationClassName) {

   Set<AnnotationAttributes> result = new LinkedHashSet<AnnotationAttributes>();
   addAttributesIfNotNull(result, metadata.getAnnotationAttributes(annotationClassName, false));

   Map<String, Object> container = metadata.getAnnotationAttributes(containerClassName, false);
   if (container != null && container.containsKey("value")) {
      for (Map<String, Object> containedAttributes : (Map<String, Object>[]) container.get("value")) {
         addAttributesIfNotNull(result, containedAttributes);
      }
   }
   return Collections.unmodifiableSet(result);
}
```



### 1. 通过给定的注解名字，获取当前注解所有的属性

**目标类：AnnotatedTypeMetadata**

```
	/**
	 * 返回一个Annotation下所有的属性。
	 * Retrieve the attributes of the annotation of the given type, if any (i.e. if
	 * defined on the underlying element, as direct annotation or meta-annotation),
	 * also taking attribute overrides on composed annotations into account.
	 * @param annotationName 注解的全限定名
	 * @param classValuesAsString 是否将一个类的引用转换为一个字符串。
	 * 因为如果是一个类的引用，需要先进行加载
	 * @return 返回一个属性的Map，如果注解中没有任何的属性进行定义，则返回null
	 */
	Map<String, Object> getAnnotationAttributes(String annotationName, boolean classValuesAsString);
```

![](http://oimj9bzzz.bkt.clouddn.com/18-8-17/88682277.jpg)