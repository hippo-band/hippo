package com.github.hippo.bean;

import java.io.Serializable;

import com.github.hippo.callback.CallType;
import com.github.hippo.callback.ICallBack;
import com.github.hippo.enums.HippoRequestEnum;


/**
 * 请求包装类
 * 
 * @author sl
 *
 */
public class HippoRequest implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 8703973176164750873L;
  private String serviceName;
  private String requestId;
  private String chainId;
  private int chainOrder;
  private Integer requestType = HippoRequestEnum.RPC.getType();// default
  private String className;
  private String methodName;
  private Class<?>[] parameterTypes;
  private Object[] parameters;
  private transient String callType = CallType.SYNC;
  private transient ICallBack iCallBack;
  private transient int timeout;

  

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

  public Integer getRequestType() {
    return requestType;
  }

  public void setRequestType(Integer requestType) {
    this.requestType = requestType;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public Class<?>[] getParameterTypes() {
    return parameterTypes;
  }

  public void setParameterTypes(Class<?>[] parameterTypes) {
    this.parameterTypes = parameterTypes;
  }

  public Object[] getParameters() {
    return parameters;
  }

  public void setParameters(Object[] parameters) {
    this.parameters = parameters;
  }


  public ICallBack getiCallBack() {
    return iCallBack;
  }

  public void setiCallBack(ICallBack iCallBack) {
    this.iCallBack = iCallBack;
  }

  public String getCallType() {
    return callType;
  }

  public void setCallType(String callType) {
    this.callType = callType;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }
}
