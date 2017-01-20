package com.github.hippo.test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.test.HippoClientBootstrapMapTest;
import com.github.hippo.test.HippoClientBootstrapTest;

import io.netty.handler.timeout.ReadTimeoutException;

public class HippoResultCallBackTest {
  private Lock lock = new ReentrantLock();
  private Condition finish = lock.newCondition();
  private int hippoReadTimeout;
  private boolean needTimeout;
  private HippoResponse hippoResponse;
  private HippoRequest hippoRequest;
  private HippoClientBootstrapTest hippoClientBootstrapTest;


  public HippoRequest getHippoRequest() {
    return hippoRequest;
  }

  public HippoResultCallBackTest(HippoRequest hippoRequest, boolean needTimeout,
      int hippoReadTimeout, HippoClientBootstrapTest hippoClientBootstrapTest) {
    this.hippoRequest = hippoRequest;
    this.needTimeout = needTimeout;
    this.hippoReadTimeout = hippoReadTimeout;
    this.hippoClientBootstrapTest = hippoClientBootstrapTest;
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
        hippoClientBootstrapTest.getReadTimeoutTimes().incrementAndGet();
      }
      if (hippoClientBootstrapTest.getReadTimeoutTimes().compareAndSet(3, 0)) {

        HippoClientBootstrapTest remove =
            HippoClientBootstrapMapTest.remove(hippoClientBootstrapTest.getserviceName());
        remove.close();
      }
      if (hippoResponse != null) {
        return hippoResponse;
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
