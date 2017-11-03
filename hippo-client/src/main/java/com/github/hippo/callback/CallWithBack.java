package com.github.hippo.callback;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.exception.HippoRequestTypeNotExistException;
import com.github.hippo.netty.HippoClientBootstrap;
import com.github.hippo.netty.HippoResultCallBack;

/**
 * Created by wangjian on 17/10/24.
 */
public class CallWithBack implements RemoteCallHandler {
  @Override
  public HippoResponse call(HippoClientBootstrap hippoClientBootstrap, HippoRequest hippoRequest,
      int timeOut) throws Exception {
    try {
      ICallBack iCallBack = hippoRequest.getiCallBack();
      if (iCallBack == null) {
        throw new HippoRequestTypeNotExistException("callback 不能为null");
      }
      return hippoClientBootstrap.sendWithCallBack(hippoRequest, timeOut);
    } finally {
      CallBackHelper.INSTANCE.remove();
    }

  }


  @Override
  public void back(HippoResultCallBack hippoResultCallBack, HippoResponse hippoResponse) {
    ICallBack callBack = hippoResultCallBack.getHippoRequest().getiCallBack();
    if (hippoResponse.isError()) {
      callBack.onFailure(hippoResponse.getThrowable());
    } else {
      callBack.onSuccess(hippoResponse.getResult());
    }


  }
}
