package com.github.hippo.hystrix;

import com.github.hippo.bean.HippoResponse;

/**
 * 降级类接口
 * 
 * @author sl
 *
 * @param <T>
 */
public interface HippoFailPolicy<T> {

  /**
   * failCallBack
   * @param hippoResponse
   * @return
   */
  T failCallBack(HippoResponse hippoResponse);
}
