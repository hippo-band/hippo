package com.github.hippo.netty;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.BeanUtils;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.exception.HippoReadTimeoutException;
/**
 * 获取hippo call back result
 * @author sl
 *
 */
public class HippoResultCallBack {
  private Lock lock = new ReentrantLock();
  private Condition finish = lock.newCondition();
  private int timeout;
  private HippoResponse hippoResponse;
  private HippoRequest hippoRequest;


  protected HippoRequest getHippoRequest() {
    return hippoRequest;
  }

  protected HippoResultCallBack(HippoRequest hippoRequest, int timeout) {
    this.hippoRequest = hippoRequest;
    this.timeout = timeout;
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
    try {
      int waitTime = timeout;
      lock.lock();
      // 最大1分钟超时
      if (waitTime <= 0) {
        waitTime = 60000;
      }
      if (!finish.await(waitTime, TimeUnit.MILLISECONDS)) {
        hippoResponse = new HippoResponse();
        BeanUtils.copyProperties(hippoRequest, hippoResponse);
        hippoResponse.setError(true);
        hippoResponse.setRequestId(hippoRequest.getRequestId());
        hippoResponse.setThrowable(
            new HippoReadTimeoutException("[" + hippoRequest + "]超时,超时时间[" + waitTime + "]毫秒"));
      }
    } catch (InterruptedException e) {
      hippoResponse = new HippoResponse();
      BeanUtils.copyProperties(hippoRequest, hippoResponse);
      hippoResponse.setError(true);
      hippoResponse.setThrowable(e);
    } finally {
      lock.unlock();
    }
    return hippoResponse;
  }
}
