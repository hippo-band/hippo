package com.github.hippo.callback;


import java.util.ArrayList;
import java.util.List;

/**
 * @author wangjian
 */

public class CallFactory {

    private static final List<RemoteCallHandler>  callHandleList = new ArrayList<>();


    static {
        callHandleList.add(new CallSync());
        callHandleList.add(new CallOneWay());
        callHandleList.add(new CallWithBack());
    }
    public static  List<RemoteCallHandler> getCallHandleList() {
        return callHandleList;
    }
}
