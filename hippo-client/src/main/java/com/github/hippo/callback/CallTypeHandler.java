package com.github.hippo.callback;

/**
 * 调用类型处理枚举类
 * 
 * @author sl
 *
 */

public enum CallTypeHandler {

  INSTANCE {
    @Override
    public RemoteCallHandler getHandler(CallType callType) {
      if (callType == CallType.ASYNC) {
        return new CallAsync();
      }
      if (callType == CallType.SYNC) {
        return new CallSync();
      }
      if (callType == CallType.ONEWAY) {
        return new CallOneWay();
      }
      return null;
    }
  };

  public abstract RemoteCallHandler getHandler(CallType callType);

}
