package com.github.hippo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * hippo client
 * 
 * @author sl
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface HippoClient {
  /**
   * 超时时间,默认5000毫秒
   * @return 超时时间
   */
  int timeout() default 5000;

  /**
   * 超时失败重试次数,默认不重试
   * @return 超时后重试次数
   */
  int retryTimes() default 0;
}
