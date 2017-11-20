package com.github.hippo.callback;

/**
 * @author wangjian
 */
public interface ICallBack {

  /**
   * 成功回调
   * @param result
   */
    void onSuccess(Object result);

    /**
     * 失败回调
     * @param e
     */
    void onFailure(Throwable e);
}
