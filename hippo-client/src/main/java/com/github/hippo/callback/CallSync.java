package com.github.hippo.callback;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.netty.HippoClientBootstrap;
import com.github.hippo.netty.HippoResultCallBack;

/**
 * Created by wangjian on 17/10/24.
 */
public class CallSync implements RemoteCallHandler {


  @Override
  public HippoResponse call(HippoClientBootstrap hippoClientBootstrap, HippoRequest hippoRequest,
      int timeOut) throws Exception {
    return hippoClientBootstrap.sendAsync(hippoRequest, timeOut).getResult();
  }


  @Override
  public void back(HippoResultCallBack hippoResultCallBack, HippoResponse hippoResponse) {
    hippoResultCallBack.signal(hippoResponse);
  }
}
