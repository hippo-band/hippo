package com.github.hippo.client;

import java.lang.reflect.Proxy;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.hippo.annotation.HippoService;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.chain.ChainThreadLocal;
import com.github.hippo.enums.HippoRequestEnum;
import com.github.hippo.govern.ServiceGovern;
import com.github.hippo.netty.HippoClientBootstrap;
import com.github.hippo.netty.HippoResultCallBack;

import io.netty.handler.timeout.ReadTimeoutException;

/**
 * client代理类
 * 
 * @author sl
 *
 */
@Component
public class HippoProxy {

  @Autowired
  private ServiceGovern serviceGovern;

  @SuppressWarnings("unchecked")
  <T> T create(Class<?> inferfaceClass, int timeout, int retryTimes) {
    return (T) Proxy.newProxyInstance(inferfaceClass.getClassLoader(),
        new Class<?>[] {inferfaceClass}, (proxy, method, args) -> {
          HippoRequest request = new HippoRequest();
          request.setRequestId(UUID.randomUUID().toString());
          request.setChainId(ChainThreadLocal.INSTANCE.getChainId());
          request.setChainOrder(ChainThreadLocal.INSTANCE.getChainOrder());
          request.setRequestType(HippoRequestEnum.RPC.getType());
          request.setClassName(method.getDeclaringClass().getName());
          request.setMethodName(method.getName());
          request.setParameterTypes(method.getParameterTypes());
          request.setParameters(args);
          String serviceName = inferfaceClass.getAnnotation(HippoService.class).serviceName();
          request.setServiceName(serviceName);
          ChainThreadLocal.INSTANCE.clearTL();
          return getHippoResponse(serviceName, request, timeout, retryTimes);
        });
  }

  private Object getHippoResponse(String serviceName, HippoRequest request, int timeout,
      int retryTimes) throws Throwable {
    // 重试次数不能大于5次
    int index = retryTimes;
    if (retryTimes >= 5) {
      index = 5;
    }
    HippoResponse result = getResult(serviceName, request, timeout);
    if (result.isError()) {
      if (result.getThrowable() instanceof ReadTimeoutException && index > 0) {
        return getHippoResponse(serviceName, request, timeout, retryTimes - 1);
      } else {
        throw result.getThrowable();
      }
    }
    return result.getResult();
  }

  private HippoResponse getResult(String serviceName, HippoRequest request, int timeout)
      throws Exception {
    HippoClientBootstrap hippoClientBootstrap =
        HippoClientBootstrap.getBootstrap(serviceName, timeout, serviceGovern);
    HippoResultCallBack callback = hippoClientBootstrap.sendAsync(request);
    return callback.getResult();
  }

  /**
   * api request 默认5秒超时,2次重试
   * 
   * @param serviceHost
   * @param serviceMethod
   * @param parameter
   * @return
   * @throws Throwable
   */
  public Object apiRequest(String serviceName, String serviceMethod, Object parameter)
      throws Throwable {
    return apiRequest(serviceName, serviceMethod, parameter, 5000, 2);
  }

  /**
   * api request
   * 
   * @param serviceName
   * @param serviceMethod
   * @param parameter
   * @param timeout 超时时间 单位毫秒
   * @param retryTimes 超时后自动重试次数
   * @return
   * @throws Throwable
   */
  public Object apiRequest(String serviceName, String serviceMethod, Object parameter, int timeout,
      int retryTimes) throws Throwable {
    String[] serviceMethods = serviceMethod.split("/");
    Object[] objects = new Object[1];
    objects[0] = parameter;
    HippoRequest request = new HippoRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setChainId(ChainThreadLocal.INSTANCE.getChainId());
    request.setChainOrder(ChainThreadLocal.INSTANCE.getChainOrder());
    request.setRequestType(HippoRequestEnum.API.getType());
    request.setClassName(serviceMethods[0]);
    request.setMethodName(serviceMethods[1]);
    request.setParameterTypes(null);
    request.setParameters(objects);
    request.setServiceName(serviceName);
    ChainThreadLocal.INSTANCE.clearTL();
    return getHippoResponse(serviceName, request, timeout, retryTimes);
  }
}
