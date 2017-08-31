package com.github.hippo.hystrix;

import com.github.hippo.bean.HippoResponse;

public interface HippoFailPolicy<T> {

	  T failCallBack(HippoResponse hippoResponse);
}
