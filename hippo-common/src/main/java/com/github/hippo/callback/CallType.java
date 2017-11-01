package com.github.hippo.callback;

/**
 * @author wangjian
 */
public interface CallType {
    /**
     * 同步
     */
    String SYNC = "sync";

    /**
     * 异步回调
     */
    String CALLBACK = "callBack";

    /**
     * 单向
     */
    String ONEWAY = "oneWay";//单向
}
