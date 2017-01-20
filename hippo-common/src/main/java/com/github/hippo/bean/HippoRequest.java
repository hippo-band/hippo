package com.github.hippo.bean;

import java.io.Serializable;
import java.util.Arrays;

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
  private String clientId;
  private String requestId;
  private String msgId;
  private int msgLevel;
  private Integer requestType = HippoRequestEnum.RPC.getType();// default
  private String className;
  private String methodName;
  private Class<?>[] parameterTypes;
  private Object[] parameters;


  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getMsgId() {
    return msgId;
  }

  public void setMsgId(String msgId) {
    this.msgId = msgId;
  }

  public int getMsgLevel() {
    return msgLevel;
  }

  public void setMsgLevel(int msgLevel) {
    this.msgLevel = msgLevel;
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

  @Override
  public String toString() {
    return "HippoRequest{" +
            "clientId='" + clientId + '\'' +
            ", requestId='" + requestId + '\'' +
            ", msgId='" + msgId + '\'' +
            ", msgLevel=" + msgLevel +
            ", requestType=" + requestType +
            ", className='" + className + '\'' +
            ", methodName='" + methodName + '\'' +
            ", parameterTypes=" + Arrays.toString(parameterTypes) +
            ", parameters=" + Arrays.toString(parameters) +
            '}';
  }
}
