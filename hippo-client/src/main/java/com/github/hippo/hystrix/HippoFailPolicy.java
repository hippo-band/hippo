package com.github.hippo.hystrix;

public interface HippoFailPolicy<T> {

	  T failCallBack(String serviceName);
}
