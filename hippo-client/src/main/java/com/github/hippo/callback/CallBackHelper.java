package com.github.hippo.callback;

/**
 * @author wangjian
 */
public enum CallBackHelper {
  INSTANCE;

  private final ThreadLocal<ICallBack> callBackLocal = new ThreadLocal<>();

  public ICallBack get() {
    return callBackLocal.get();
  }

  public void set(ICallBack iCallBack) {
    if (iCallBack != null) {
      callBackLocal.set(iCallBack);
    }
  }

  public void remove() {
    if (callBackLocal.get() != null) {
      callBackLocal.remove();
    }
  }
}
