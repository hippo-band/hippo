package com.github.hippo.callback;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.netty.HippoClientBootstrap;
import com.github.hippo.netty.HippoResultCallBack;

/**
 * Created by wangjian on 17/10/24.
 */
public interface RemoteCallHandler {

  HippoResponse call(HippoClientBootstrap HippoClientBootstrap, HippoRequest hippoRequest,
      int timeOut) throws Exception;

  void back(HippoResultCallBack hippoResultCallBack, HippoResponse hippoResponse);
}
