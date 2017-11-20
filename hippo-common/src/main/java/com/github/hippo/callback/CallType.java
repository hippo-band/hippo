package com.github.hippo.callback;

/**
 * 调用工厂枚举类
 * 
 * @author sl
 *
 */

public enum CallType {

  
  SYNC("同步调用"), ONEWAY("单向发送不需要拿返回结果"), ASYNC("异步调用,可以从callback拿返回结果");

  private String desc;

  CallType(String desc) {
    this.desc = desc;
  }

  public String getDesc() {
    return desc;
  }


}
