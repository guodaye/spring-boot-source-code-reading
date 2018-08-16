### 一、AopProxy

这是实现spring动态代理的基础，spring通过将真实的Bean封装为一个AopProxy。

![aop](http://oimj9bzzz.bkt.clouddn.com/18-8-16/84143474.jpg)



### 二、自动化注入AnnotationAwareAspectJAutoProxyCreator

![](http://oimj9bzzz.bkt.clouddn.com/18-8-16/97848224.jpg)



### 三、

![](http://oimj9bzzz.bkt.clouddn.com/18-8-16/13534933.jpg)

当存在`EnableAspectJAutoProxy`这个类的时候，会初始化这个类



### 四、

![](http://oimj9bzzz.bkt.clouddn.com/18-8-16/87688675.jpg)