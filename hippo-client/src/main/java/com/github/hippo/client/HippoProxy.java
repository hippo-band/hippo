package com.github.hippo.client;

import java.lang.reflect.Proxy;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.hippo.annotation.HippoClient;
import com.github.hippo.annotation.HippoService;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.chain.ChainThreadLocal;
import com.github.hippo.enums.HippoRequestEnum;
import com.github.hippo.govern.ServiceGovern;
import com.github.hippo.hystrix.HippoCommand;

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
	<T> T create(Class<?> inferfaceClass, HippoClient hippoClient) {
		return (T) Proxy.newProxyInstance(inferfaceClass.getClassLoader(), new Class<?>[] { inferfaceClass },
				(proxy, method, args) -> {
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
					HippoCommand hippoCommand = new HippoCommand(request, hippoClient.timeout(),
							hippoClient.retryTimes(), hippoClient.isCircuitBreaker(),
							hippoClient.semaphoreMaxConcurrentRequests(), hippoClient.downgradeStrategy(),
							serviceGovern);
					if (hippoClient.isUseHystrix()) {
						return hippoCommand.execute();
					} else {
						return hippoCommand.getHippoResponse(request, hippoClient.timeout(), hippoClient.retryTimes());
					}
				});
	}

	public Object apiRequest(String serviceName, String serviceMethod, Object parameter, int timeout, int retryTimes,
			boolean isCircuitBreaker, int semaphoreMaxConcurrentRequests, Class<?> hippoFailPolicy) throws Throwable {
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
       
		HippoCommand hippoCommand = new HippoCommand(request, timeout, retryTimes, isCircuitBreaker,
				semaphoreMaxConcurrentRequests == 0 ? 10 : semaphoreMaxConcurrentRequests, Void.class, serviceGovern);
		return hippoCommand.execute();
	}

	public Object apiRequest(String serviceName, String serviceMethod, Object parameter) throws Throwable {
		return apiRequest(serviceName, serviceMethod, parameter, 5000, 2, false, 10, Void.class);
	}

}
