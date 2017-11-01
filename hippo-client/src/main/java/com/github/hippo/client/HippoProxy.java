package com.github.hippo.client;

import java.lang.reflect.Proxy;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.hippo.annotation.HippoClient;
import com.github.hippo.annotation.HippoService;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.callback.CallBackHelper;
import com.github.hippo.callback.ICallBackBean;
import com.github.hippo.chain.ChainThreadLocal;
import com.github.hippo.enums.HippoRequestEnum;
import com.github.hippo.govern.ServiceGovern;
import com.github.hippo.hystrix.HippoCommand;
import com.github.hippo.netty.HippoClientBootstrapMap;

/**
 * client代理类
 *
 * @author sl
 */
@Component
public class HippoProxy {

    @Autowired
    private ServiceGovern serviceGovern;

    @SuppressWarnings("unchecked")
    <T> T create(Class<?> inferfaceClass, HippoClient hippoClient) {
        return (T) Proxy.newProxyInstance(inferfaceClass.getClassLoader(),
                new Class<?>[]{inferfaceClass}, (proxy, method, args) -> {
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
                    ICallBackBean callBackBean = CallBackHelper.Instance.get();
                    if (callBackBean != null) {
                        request.setCallType(callBackBean.getCallType());
                        request.setiCallBack(callBackBean.getiCallBack());
                    }
                    ChainThreadLocal.INSTANCE.clearTL();
                    HippoCommand hippoCommand =
                            new HippoCommand(request, hippoClient.timeout(), hippoClient.retryTimes(),
                                    hippoClient.isCircuitBreaker(), hippoClient.semaphoreMaxConcurrentRequests(),
                                    hippoClient.downgradeStrategy(), hippoClient.fallbackEnabled());
                    HippoResponse hippoResponse;
                    // 由于长连接是由定时器线程去持续获得,那如果是junit或者有些请求已经到来也需要获取连接来处理数据
                    if (HippoClientBootstrapMap.get(serviceName) == null) {
                        conntectionOne(serviceName);
                    }
                    if (hippoClient.isUseHystrix()) {
                        hippoResponse = (HippoResponse) hippoCommand.execute();
                    } else {
                        hippoResponse = hippoCommand.getHippoResponse(request, hippoClient.timeout(),
                                hippoClient.retryTimes());
                    }
                    if (hippoResponse.isError()) {
                        throw hippoResponse.getThrowable();
                    } else {
                        return hippoResponse.getResult();
                    }
                });
    }

    private void conntectionOne(String serviceName) throws Exception {
        String serviceAddresse = serviceGovern.getServiceAddress(serviceName);
        if (StringUtils.isBlank(serviceAddresse)) {
            return;
        }
        String[] split = serviceAddresse.split(":");
        String host = split[0];
        int port = Integer.parseInt(split[1]);
        if (StringUtils.isBlank(host) || port <= 0 || port > 65532) {
            return;
        }
        HippoClientInit.createHippoHandler(serviceName, host, port);
    }


    public Object apiRequest(String serviceName, String serviceMethod, Object parameter, int timeout,
                             int retryTimes, boolean isCircuitBreaker, int semaphoreMaxConcurrentRequests,
                             boolean fallbackEnable, Class<?> hippoFailPolicy) throws Throwable {
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
        ICallBackBean callBackBean = CallBackHelper.Instance.get();
        if (callBackBean != null) {
            request.setCallType(callBackBean.getCallType());
            request.setiCallBack(callBackBean.getiCallBack());
        }
        ChainThreadLocal.INSTANCE.clearTL();

        HippoCommand hippoCommand = new HippoCommand(request, timeout, retryTimes, isCircuitBreaker,
                semaphoreMaxConcurrentRequests == 0 ? 10 : semaphoreMaxConcurrentRequests, hippoFailPolicy,
                fallbackEnable);
        HippoResponse hippoResponse = (HippoResponse) hippoCommand.execute();
        if (hippoResponse.isError()) {
            throw hippoResponse.getThrowable();
        } else {
            return hippoResponse.getResult();
        }
    }

    public Object apiRequest(String serviceName, String serviceMethod, Object parameter)
            throws Throwable {
        return apiRequest(serviceName, serviceMethod, parameter, 5000, 0, true, 10, false, null);
    }

}
