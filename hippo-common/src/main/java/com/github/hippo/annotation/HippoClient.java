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
	 * 
	 * @return
	 */
	int timeout() default 5000;

	/**
	 * 超时失败重试次数,默认不重试
	 * 
	 * @return
	 */
	int retryTimes() default 0;

	/**
	 * 是否启用hystrix
	 * 默认不启用hystrix
	 * @return
	 */
	boolean isUseHystrix() default false;

	/**
	 * 熔断开关
	 * 默认关闭熔断
	 * @return
	 */
	boolean isCircuitBreaker() default false;

	/**
	 * 资源隔离 防止调用provider 将consumer资源耗尽
	 * 
	 * @return
	 */
	int semaphoreMaxConcurrentRequests() default 10;

	/**
	 * 降级策略
	 * 
	 * @return
	 */
	Class<?> downgradeStrategy() default Void.class;

}
