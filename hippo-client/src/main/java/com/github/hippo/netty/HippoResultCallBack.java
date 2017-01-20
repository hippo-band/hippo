package com.github.hippo.netty;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;

import io.netty.handler.timeout.ReadTimeoutException;

public class HippoResultCallBack {
  private Lock lock = new ReentrantLock();
  private Condition finish = lock.newCondition();
  private int hippoReadTimeout;
  private boolean needTimeout;
  private HippoResponse hippoResponse;
  private HippoRequest hippoRequest;
  private AtomicInteger readTimeoutTimes;
  private String serviceName;


  public HippoRequest getHippoRequest() {
    return hippoRequest;
  }

  public HippoResultCallBack(HippoRequest hippoRequest, boolean needTimeout, int hippoReadTimeout,
      AtomicInteger readTimeoutTimes, String serviceName) {
    this.hippoRequest = hippoRequest;
    this.needTimeout = needTimeout;
    this.hippoReadTimeout = hippoReadTimeout;
    this.readTimeoutTimes = readTimeoutTimes;
    this.serviceName = serviceName;
  }

  public void signal(HippoResponse hippoResponse) {
    this.hippoResponse = hippoResponse;
    try {
      lock.lock();
      finish.signal();
    } finally {
      lock.unlock();
    }
  }

  public HippoResponse getResult() {
    try {
      lock.lock();
      int waitTime = hippoReadTimeout;
      // 最大1分钟超时
      if (!needTimeout) {
        waitTime = 60;
      }
      if (!finish.await(waitTime, TimeUnit.SECONDS)) {
        readTimeoutTimes.incrementAndGet();
      }
      if (hippoResponse != null) {
        return hippoResponse;
      }
      if (readTimeoutTimes.compareAndSet(6, 0)) {
        HippoClientBootstrapMap.remove(serviceName).close();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
    }
    hippoResponse = new HippoResponse();
    hippoResponse.setError(true);
    hippoResponse.setRequestId(hippoRequest.getRequestId());
    hippoResponse.setThrowable(ReadTimeoutException.INSTANCE);

    return hippoResponse;
  }
}
