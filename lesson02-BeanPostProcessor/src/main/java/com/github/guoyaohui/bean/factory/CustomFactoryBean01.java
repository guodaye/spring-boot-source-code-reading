package com.github.guoyaohui.bean.factory;

import com.github.guoyaohui.bean.CustomBean01;
import com.github.guoyaohui.bean.ICustomBean;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * bean默认是懒加载的
 *
 * @author 郭垚辉
 * @date 2018/08/15
 */
//@Component
public class CustomFactoryBean01 extends AbstractFactoryBean<ICustomBean> {

    @PostConstruct
    public void init() {
        System.out.println("CustomFactoryBean01： 无他，只是为了查看下这个类何时添加的");
    }

    @Override
    public Class<?> getObjectType() {
        return ICustomBean.class;
    }

    protected ICustomBean createInstance() {
        boolean exist = getBeanFactory().containsBean("iCustomBean");
        return exist ? getBeanFactory().getBean(ICustomBean.class) : new CustomBean01();
    }
}
