package com.github.hippo.bean;

import java.io.Serializable;


/**
 * 
 * 返回结果包装类
 * 
 * @author sl
 *
 */
public class HippoResponse implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -2553246569725890161L;
  private String requestId;
  private String serviceName;
  private String chainId;
  private int chainOrder;
  private Object result;
  private Throwable throwable;
  private boolean isError = false;

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }


  public String getChainId() {
    return chainId;
  }

  public void setChainId(String chainId) {
    this.chainId = chainId;
  }

  public int getChainOrder() {
    return chainOrder;
  }

  public void setChainOrder(int chainOrder) {
    this.chainOrder = chainOrder;
  }

  public Object getResult() {
    return result;
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  public boolean isError() {
    return isError;
  }

  public void setError(boolean isError) {
    this.isError = isError;
  }

  @Override
  public String toString() {
    String logResult = "null";
    if (result != null) {
      int index = result.toString().length();
      if (index > 150) {
        logResult = result.toString().substring(0, 150);
      } else {
        logResult = result.toString();
      }
    }
    return "HippoResponse [requestId=" + requestId + ", serviceName=" + serviceName + ", chainId="
        + chainId + ", chainOrder=" + chainOrder + ", result=" + logResult + ", throwable="
        + throwable + ", isError=" + isError + "]";
  }



}
