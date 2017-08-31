package com.github.hippo.hystrix;

import com.github.hippo.bean.HippoResponse;
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
  public Object failCallBack(HippoResponse hippoResponse) {
    throw new HippoRuntimeException("call[" + hippoResponse.getServiceName() + "]异常且触发降级服务",
        hippoResponse.getThrowable());
  }

}
