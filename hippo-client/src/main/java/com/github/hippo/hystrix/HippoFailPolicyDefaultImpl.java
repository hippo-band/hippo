package com.github.hippo.hystrix;

import com.github.hippo.exception.HippoRuntimeException;

/**
 * 熔断后的默认实现
 * 
 * @author wj
 * 
 *
 */
public class HippoFailPolicyDefaultImpl implements HippoFailPolicy<Object> {

  @Override
  public Object failCallBack(String serviceName) {
    // 默认实现 会导致多抛HystrixRuntimeException 因为hystrix的本意是mock默认数据返回，还不是抛出异常，
    // 但是默认实现不能预料到调用者所用的数据类型,所以只能抛出一个服务不可用的异常
    throw new HippoRuntimeException("已启动默认降级策略,调用[" + serviceName + "]服务不可用");
  }

}
