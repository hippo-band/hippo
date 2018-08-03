package com.github.hippo.annotation;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明在实现类
 * 打个标记以及componentScan
 * @author sl
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@ComponentScan(basePackages = "com.github.hippo")
public @interface HippoServiceImpl {
}
