package com.github.hippo.callback;

/**
 * @author wangjian
 */
public enum CallTypeHelper {
  SETTING {
    @Override
    public void oneway() {
      set(new ICallBackBean(null, CallType.ONEWAY));
    }

    @Override
    public void async(ICallBack iCallBack) {
      set(new ICallBackBean(iCallBack, CallType.ASYNC));
    }
  };

  private final ThreadLocal<ICallBackBean> callBackLocal = new ThreadLocal<>();


  public abstract void oneway();

  public abstract void async(ICallBack iCallBack);

  public ICallBackBean get() {
    return callBackLocal.get();
  }

  void set(ICallBackBean iCallBackBean) {
    if (iCallBackBean != null) {
      callBackLocal.set(iCallBackBean);
    }
  }


  public void remove() {
    if (callBackLocal.get() != null) {
      callBackLocal.remove();
    }
  }
}
