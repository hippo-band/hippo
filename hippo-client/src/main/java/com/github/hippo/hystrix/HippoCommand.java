package com.github.hippo.hystrix;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.client.HippoClientInit;
import com.github.hippo.exception.HippoReadTimeoutException;
import com.github.hippo.govern.ServiceGovern;
import com.github.hippo.netty.HippoClientBootstrap;
import com.github.hippo.netty.HippoResultCallBack;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * hystrix
 * 
 * @author wj
 *
 */

public class HippoCommand extends HystrixCommand<Object> {

  private HippoRequest hippoRequest;

  private int timeOut;

  private int retryTimes;

  private ServiceGovern serviceGovern;

  private HippoFailPolicy<?> hippoFailPolicy;

  public HippoCommand(HippoRequest hippoRequest, int timeOut, int retryTimes,
      boolean isCircuitBreaker, int semaphoreMaxConcurrentRequests, Class<?> downgradeStrategy,
      boolean fallbackEnabled, ServiceGovern serviceGovern)
      throws InstantiationException, IllegalAccessException {

    // 默认隔离策略是线程 也可以是信号量,现在采用的是信号量的模式
    // 信号量隔离是个限流的策略
    // 因为是自己实现的超时机制，所以关闭hystrix的超时机制
    super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(hippoRequest.getServiceName()))
        .andCommandKey(HystrixCommandKey.Factory.asKey(hippoRequest.getClassName()))
        .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
            .withExecutionIsolationStrategy(
                HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)
            .withExecutionIsolationSemaphoreMaxConcurrentRequests(semaphoreMaxConcurrentRequests)
            .withFallbackEnabled(fallbackEnabled).withCircuitBreakerEnabled(isCircuitBreaker)
            .withExecutionTimeoutEnabled(false)));

    this.hippoRequest = hippoRequest;
    this.timeOut = timeOut;
    this.retryTimes = retryTimes;
    this.serviceGovern = serviceGovern;
    if (fallbackEnabled) {
      init(downgradeStrategy);
    }
  }

  private void init(Class<?> downgradeStrategy)
      throws InstantiationException, IllegalAccessException {
    if (downgradeStrategy == null) {
      hippoFailPolicy = new HippoFailPolicyDefaultImpl();
    } else {
      if (HippoFailPolicy.class.isAssignableFrom(downgradeStrategy)) {
        // 先从spring容器里面拿，如果没有，则new
        try {
          Object bean = HippoClientInit.getApplicationContext().getBean(downgradeStrategy);
          hippoFailPolicy = (HippoFailPolicy<?>) bean;
        } catch (Exception e) {
          hippoFailPolicy = (HippoFailPolicy<?>) downgradeStrategy.newInstance();
        }
      } else {
        hippoFailPolicy = new HippoFailPolicyDefaultImpl();
      }
    }
  }

  @Override
  protected Object run() throws Exception {
    try {
      return getHippoResponse(hippoRequest, timeOut, retryTimes);
    } catch (Exception e) {
      throw e;
    }
  }


  @Override
  protected Object getFallback() {
    return hippoFailPolicy.failCallBack(hippoRequest.getServiceName());
  }

  public HippoResponse getHippoResponse(HippoRequest request, int timeout, int retryTimes)
      throws Exception {

    // 重试次数不能大于5次
    int index = retryTimes;
    if (retryTimes >= 5) {
      index = 5;
    }
    HippoResponse result = getResult(request, timeout);
    if (result.isError() && result.getThrowable() instanceof HippoReadTimeoutException
        && index > 0) {
      return getHippoResponse(request, timeout, retryTimes - 1);
    }
    return result;
  }

  private HippoResponse getResult(HippoRequest request, int timeout) throws Exception {

    HippoClientBootstrap hippoClientBootstrap =
        HippoClientBootstrap.getBootstrap(request.getServiceName(), timeout, serviceGovern);
    HippoResultCallBack callback = hippoClientBootstrap.sendAsync(request);
    return callback.getResult();
  }

}
