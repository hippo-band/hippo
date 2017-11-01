package com.github.hippo.callback;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.netty.HippoClientBootstrap;
import com.github.hippo.netty.HippoResultCallBack;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by wangjian on 17/10/24.
 */
public class CallSync implements RemoteCallHandler {


    @Override
    public HippoResponse call(HippoClientBootstrap hippoClientBootstrap, HippoRequest hippoRequest,int timeOut) throws Exception {
        return hippoClientBootstrap.sendAsync(hippoRequest,timeOut).getResult();
    }

    @Override
    public boolean canProcess(String callType) {

        return StringUtils.isNotBlank(callType) && CallType.SYNC.equals(callType);
    }

    @Override
    public void back(HippoResultCallBack hippoResultCallBack, HippoResponse hippoResponse) {
        hippoResultCallBack.signal(hippoResponse);
    }
}
