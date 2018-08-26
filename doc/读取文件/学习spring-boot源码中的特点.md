当多个实现类实现了同一个接口，这时候，需要添加注解

```
@EnableAspectJAutoProxy(proxyTargetClass = true)
```

否则对报错