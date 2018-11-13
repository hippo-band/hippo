package com.github.hippo.client;

import com.github.hippo.annotation.HippoClient;
import com.github.hippo.annotation.HippoService;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.callback.CallTypeHelper;
import com.github.hippo.callback.ICallBackBean;
import com.github.hippo.chain.ChainThreadLocal;
import com.github.hippo.enums.HippoRequestEnum;
import com.github.hippo.govern.ServiceGovern;
import com.github.hippo.hystrix.HippoCommand;
import com.github.hippo.netty.HippoClientBootstrapMap;
import com.github.hippo.zipkin.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * client代理类
 *
 * @author sl
 */
@Component
public class HippoProxy {

    @Autowired
    private ServiceGovern serviceGovern;

    @Value("${service.name:}")
    private String currentServiceName;

    @Autowired(required = false)
    private ZipkinRecordService zipkinRecordService;

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
                    HippoService annotation = inferfaceClass.getAnnotation(HippoService.class);
                    String serviceName;
                    if (annotation != null) {
                        serviceName = annotation.serviceName();
                    } else if (StringUtils.isNotBlank(hippoClient.serviceName())) {
                        serviceName = hippoClient.serviceName();
                    } else {
                        serviceName = getServiceNameByClassName(request.getClassName());
                    }

                    request.setServiceName(serviceName);
                    ICallBackBean callBack = CallTypeHelper.SETTING.get();
                    if (callBack != null) {
                        request.setiCallBack(callBack.getiCallBack());
                        request.setCallType(callBack.getCallType());
                    }
                    return commonInvoke(request, hippoClient.timeout(), hippoClient.retryTimes(),
                            hippoClient.isCircuitBreaker(), hippoClient.semaphoreMaxConcurrentRequests(),
                            hippoClient.downgradeStrategy(), hippoClient.fallbackEnabled(), hippoClient.isUseHystrix(), currentServiceName);
                });
    }

    private String getServiceNameByClassName(String className) {
        if (StringUtils.isBlank(className)) {
            return null;
        }
        if (StringUtils.isBlank(HippoClientCache.INSTANCE.getClassNameServiceNameMap().get(className))) {
            String serviceNameByClassName = serviceGovern.getServiceNameByClassName(className);
            HippoClientCache.INSTANCE.getClassNameServiceNameMap().put(className, serviceNameByClassName);
        }
        return HippoClientCache.INSTANCE.getClassNameServiceNameMap().get(className);
    }

    private void conntectionOne(String serviceName) {
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
        ICallBackBean callBack = CallTypeHelper.SETTING.get();
        if (callBack != null) {
            request.setiCallBack(callBack.getiCallBack());
            request.setCallType(callBack.getCallType());
        }
        return commonInvoke(request, timeout, retryTimes, isCircuitBreaker,
                semaphoreMaxConcurrentRequests == 0 ? 10 : semaphoreMaxConcurrentRequests, hippoFailPolicy,
                fallbackEnable, isCircuitBreaker, currentServiceName);
    }

    private Object commonInvoke(HippoRequest request, int timeout, int retryTimes, boolean isCircuitBreaker, int semaphoreMaxConcurrentRequests, Class<?> hippoFailPolicy, boolean fallbackEnable, boolean isUseHystrix, String currentServiceName) throws Throwable {
        ZipkinData zipkinData = null;
        if (zipkinRecordService != null) {
            zipkinData = fillZipkinData(request, currentServiceName);
        }
        ZipkinResp zipkinResp = ZipkinUtils.zipkinRecordStart(zipkinData, zipkinRecordService);
        if (zipkinResp != null) {
            request.setChainId(zipkinResp.getParentTraceId());
            request.setRequestId(zipkinResp.getParentSpanId());
        }
        ChainThreadLocal.INSTANCE.clearTL();
        HippoCommand hippoCommand =
                new HippoCommand(request, timeout, retryTimes,
                        isCircuitBreaker, semaphoreMaxConcurrentRequests,
                        hippoFailPolicy, fallbackEnable);
        HippoResponse hippoResponse;
        // 由于长连接是由定时器线程去持续获得,那如果是junit或者有些请求已经到来也需要获取连接来处理数据
        if (HippoClientBootstrapMap.get(request.getServiceName()) == null
                || HippoClientBootstrapMap.get(request.getServiceName()).isEmpty()) {
            conntectionOne(request.getServiceName());
        }
        try {
            if (isUseHystrix || isCircuitBreaker) {
                hippoResponse = (HippoResponse) hippoCommand.execute();
            } else {
                hippoResponse = hippoCommand.getHippoResponse(request, timeout,
                        retryTimes);

            }
        } catch (Exception e) {
            ZipkinUtils.zipkinRecordError(zipkinRecordService, e);
            ZipkinUtils.zipkinRecordFinish(zipkinRecordService);
            throw e;
        }
        if (hippoResponse.isError()) {
            ZipkinUtils.zipkinRecordError(zipkinRecordService, hippoResponse.getThrowable());
            ZipkinUtils.zipkinRecordFinish(zipkinRecordService);
            throw hippoResponse.getThrowable();
        } else {
            ZipkinUtils.zipkinRecordFinish(zipkinRecordService);
            return hippoResponse.getResult();
        }
    }

    private ZipkinData fillZipkinData(HippoRequest request, String currentServiceName) {
        ZipkinData zipkinData = new ZipkinData();

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String className;
        String methodName;
        if (request.getRequestType() == HippoRequestEnum.API.getType()) {
            className = stackTrace[5].getClassName();
            methodName = stackTrace[5].getMethodName();
        } else {
            className = stackTrace[4].getClassName();
            methodName = stackTrace[4].getMethodName();
        }
        zipkinData.setServiceName(StringUtils.isBlank(currentServiceName) ? className + ":" + methodName : currentServiceName);
        zipkinData.setMethodName(methodName);
        zipkinData.setSpanKind(SpanKind.CLIENT);
        zipkinData.setAnnotate(ToStringBuilder.reflectionToString(request.getParameters()));
        Map<String, String> tagMap = new HashMap<>();
        tagMap.put("className", className);
        tagMap.put("methodName", methodName);
        tagMap.put("callType", request.getCallType().name());
        tagMap.put("nextServiceName", request.getServiceName());
        tagMap.put("nextClassName", request.getClassName());
        tagMap.put("nextMethodName", request.getMethodName());
        if (StringUtils.isBlank(currentServiceName)) {
            tagMap.put("tips", "可在配置文件里配置service.name代替类名+方法名");
        }
        zipkinData.setTags(tagMap);
        if (request.getChainOrder() != 1) {
            zipkinData.setParentSpanId(Long.parseLong(request.getRequestId(), 16));
            zipkinData.setParentTraceId(Long.parseLong(request.getChainId(), 16));
        }
        return zipkinData;
    }

    public Object apiRequest(String serviceName, String serviceMethod, Object parameter)
            throws Throwable {
        return apiRequest(serviceName, serviceMethod, parameter, 5000, 0, true, 10, false, null);
    }

}
