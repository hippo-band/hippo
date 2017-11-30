package com.github.hippo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * 声明在实现类上,interfaceClass传接口class
 * 
 * @author sl
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
@ComponentScan(basePackages = "com.github.hippo")
public @interface HippoServiceImpl {
  /**
   * 接口类class 最新修改是不用传接口.class的,为了兼容老版本+个Default值并声明过期,在未来的某个版本就去掉
   * 
   * @return 接口class
   */
  @Deprecated
  Class<? extends Object> value() default Void.class;
}
