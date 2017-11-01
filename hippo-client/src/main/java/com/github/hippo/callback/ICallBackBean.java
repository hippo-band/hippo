package com.github.hippo.callback;

/**
 * Created by longie on 17/10/23.
 */
public class ICallBackBean {

    private ICallBack iCallBack;

    /**
     *  {@link CallType}
     */
    private  String callType = CallType.SYNC;

    public ICallBackBean(ICallBack iCallBack, String callType) {
        this.iCallBack = iCallBack;
        this.callType = callType;
    }


    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public ICallBack getiCallBack() {
        return iCallBack;
    }

    public void setiCallBack(ICallBack iCallBack) {
        this.iCallBack = iCallBack;
    }
}
