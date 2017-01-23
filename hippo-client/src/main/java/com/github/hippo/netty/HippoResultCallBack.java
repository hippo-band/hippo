package com.github.hippo.netty;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.exception.HippoReadTimeoutException;

public class HippoResultCallBack {
  private Lock lock = new ReentrantLock();
  private Condition finish = lock.newCondition();
  private int timeout;
  private HippoResponse hippoResponse;
  private HippoRequest hippoRequest;
  private AtomicInteger readTimeoutTimes;
  private String serviceName;


  protected HippoRequest getHippoRequest() {
    return hippoRequest;
  }

  protected HippoResultCallBack(HippoRequest hippoRequest, int timeout,
      AtomicInteger readTimeoutTimes, String serviceName) {
    this.hippoRequest = hippoRequest;
    this.timeout = timeout;
    this.readTimeoutTimes = readTimeoutTimes;
    this.serviceName = serviceName;
  }

  protected void signal(HippoResponse hippoResponse) {
    this.hippoResponse = hippoResponse;
    try {
      lock.lock();
      finish.signal();
    } finally {
      lock.unlock();
    }
  }

  public HippoResponse getResult() {
    int waitTime = timeout;
    try {
      lock.lock();
      // 最大1分钟超时
      if (waitTime <= 0) {
        waitTime = 60000;
      }
      if (!finish.await(waitTime, TimeUnit.MILLISECONDS)) {
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
    hippoResponse.setThrowable(
        new HippoReadTimeoutException("[" + hippoRequest + "]超时,超时时间[" + waitTime + "]毫秒"));
    return hippoResponse;
  }
}
