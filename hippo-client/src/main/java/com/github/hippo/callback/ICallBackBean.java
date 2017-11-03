package com.github.hippo.callback;

/**
 * Created by longie on 17/10/23.
 */
public class ICallBackBean {

  private ICallBack iCallBack;

  /**
   * {@link CallTypeHandler}
   */
  private CallType callType = CallType.SYNC;

  public ICallBackBean(ICallBack iCallBack, CallType callType) {
    this.iCallBack = iCallBack;
    this.callType = callType;
  }



  public CallType getCallType() {
    return callType;
  }



  public void setCallType(CallType callType) {
    this.callType = callType;
  }



  public ICallBack getiCallBack() {
    return iCallBack;
  }

  public void setiCallBack(ICallBack iCallBack) {
    this.iCallBack = iCallBack;
  }
}
