package com.github.hippo.callback;

/**
 * @author wangjian
 */
public enum CallBackHelper {
    Instance;

    private final ThreadLocal<ICallBackBean> callBackLocal = new ThreadLocal<>();

    public ICallBackBean get() {
        return callBackLocal.get();
    }

    public void set(ICallBackBean iCallBackBean) {
        if(iCallBackBean != null) {
            callBackLocal.set(iCallBackBean);
        }
    }

    public void remove() {
       if(callBackLocal.get()!=null) {
           callBackLocal.remove();
       }
    }
}
