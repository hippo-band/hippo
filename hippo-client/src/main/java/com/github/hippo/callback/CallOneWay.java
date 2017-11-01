package com.github.hippo.callback;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.netty.HippoClientBootstrap;
import com.github.hippo.netty.HippoResultCallBack;
import org.apache.commons.lang3.StringUtils;

/**
 * @author
 */
public class CallOneWay implements RemoteCallHandler {
    @Override
    public void back(HippoResultCallBack hippoResultCallBack, HippoResponse hippoResponse) {
        return;
    }

    @Override
    public HippoResponse call(HippoClientBootstrap hippoClientBootstrap, HippoRequest hippoRequest,int timeOut) throws Exception {
        return hippoClientBootstrap.sendOneWay(hippoRequest);
    }

    @Override
    public boolean canProcess(String callType) {
        return StringUtils.isNotBlank(callType) && CallType.ONEWAY.equals(callType);
    }
}
