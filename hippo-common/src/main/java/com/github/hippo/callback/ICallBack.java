package com.github.hippo.callback;

/**
 * @author wangjian
 */
public interface ICallBack {

    void onSuccess(Object result);

    void onFailure(Throwable e);
}
