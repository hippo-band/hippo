package com.github.hippo.server;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.chain.ChainThreadLocal;
import com.github.hippo.enums.HippoRequestEnum;
import com.github.hippo.exception.HippoRequestTypeNotExistException;
import com.github.hippo.exception.HippoServiceException;
import com.github.hippo.util.CommonUtils;
import com.github.hippo.util.FastJsonConvertUtils;
import com.github.hippo.zipkin.SpanKind;
import com.github.hippo.zipkin.ZipkinData;
import com.github.hippo.zipkin.ZipkinRecordService;
import com.github.hippo.zipkin.ZipkinUtils;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * netty handler处理类
 *
 * @author sl
 */
@Sharable
class HippoServerHandler extends SimpleChannelInboundHandler<HippoRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HippoServerHandler.class);

    private void handle(ChannelHandlerContext ctx, HippoRequest request) {
        long start = System.currentTimeMillis();
        HippoResponse response = new HippoResponse();
        response.setChainId(request.getChainId());
        response.setChainOrder(request.getChainOrder());
        response.setServiceName(request.getServiceName());

        HippoRequestEnum hippoRequestEnum = HippoRequestEnum.getByType(request.getRequestType());
        if (hippoRequestEnum != HippoRequestEnum.PING) {
            LOGGER.info("hippo start chainId:{},in param:{}", request.getChainId(), ToStringBuilder.reflectionToString(request));
            zipkinStart(request);
        }
        try {
            ChainThreadLocal.INSTANCE.setChainId(request.getChainId());
            ChainThreadLocal.INSTANCE.incChainOrder(request.getChainOrder());
            response.setRequestId(request.getRequestId());
            if (hippoRequestEnum == null) {
                response.setError(true);
                response.setThrowable(new HippoRequestTypeNotExistException(
                        "HippoRequest requestType not exist.current requestType is:"
                                + request.getRequestType()));
            } else if (hippoRequestEnum == HippoRequestEnum.API) {
                response.setResult(apiProcess(request));
            } else if (hippoRequestEnum == HippoRequestEnum.RPC) {
                response.setResult(rpcProcess(request));
            } else if (hippoRequestEnum == HippoRequestEnum.PING) {
                response.setResult("pong");
                response.setRequestId("-99");
            }
        } catch (Exception e1) {
            LOGGER.error("handle error:" + request, e1);
            if (e1 instanceof InvocationTargetException) {
                response.setThrowable(e1.getCause());
            } else {
                response.setThrowable(e1);
            }
            response.setRequestId(request.getRequestId());
            response.setResult(request);
            response.setError(true);
        }
        ChainThreadLocal.INSTANCE.clearTL();
        if (hippoRequestEnum != HippoRequestEnum.PING) {
            zipkinFinish(response);
            LOGGER.info("hippo end chainId:{} out result:{},耗时:{}毫秒", request.getChainId(), response, System.currentTimeMillis() - start);
        }

        ctx.writeAndFlush(response);
    }

    private void zipkinFinish(HippoResponse response) {
        ZipkinCache zipkinCache = HippoServiceCache.INSTANCE.getZipkinCache();
        if (StringUtils.isNotBlank(zipkinCache.getServiceName()) && zipkinCache.getZipkinRecordService() != null) {
            ZipkinRecordService zipkinRecordService = zipkinCache.getZipkinRecordService();
            if (response.isError()) {
                ZipkinUtils.zipkinRecordError(zipkinRecordService, response.getThrowable());
            }
            ZipkinUtils.zipkinRecordFinish(zipkinRecordService);
        }
    }

    private void zipkinStart(HippoRequest request) {
        ZipkinCache zipkinCache = HippoServiceCache.INSTANCE.getZipkinCache();
        if (StringUtils.isNotBlank(zipkinCache.getServiceName()) && zipkinCache.getZipkinRecordService() != null) {
            String currentServiceName = zipkinCache.getServiceName();
            ZipkinRecordService zipkinRecordService = zipkinCache.getZipkinRecordService();
            ZipkinData zipkinData = fillZipkinData(request, currentServiceName);
            ZipkinUtils.zipkinRecordStart(zipkinData, zipkinRecordService);
        }
    }

    private ZipkinData fillZipkinData(HippoRequest request, String currentServiceName) {
        ZipkinData zipkinData = new ZipkinData();
        zipkinData.setServiceName(currentServiceName);
        zipkinData.setMethodName(request.getMethodName());
        zipkinData.setSpanKind(SpanKind.SERVER);
        zipkinData.setAnnotate(ToStringBuilder.reflectionToString(request.getParameters()));
        Map<String, String> tagMap = new HashMap<>();
        tagMap.put("className", request.getClassName());
        tagMap.put("methodName", request.getMethodName());
        tagMap.put("requestType", HippoRequestEnum.getByType(request.getRequestType()).name());
        zipkinData.setTags(tagMap);

        zipkinData.setParentSpanId(new BigInteger(request.getRequestId(), 16).longValue());
        zipkinData.setParentTraceId(new BigInteger(request.getChainId(), 16).longValue());
        return zipkinData;
    }

    private Object rpcProcess(HippoRequest paras) throws InvocationTargetException {
        return HippoServiceCache.INSTANCE.getImplClassMap().get(paras.getClassName()).getMethod(paras.getMethodName(), paras.getParameterTypes()).invoke(HippoServiceCache.INSTANCE.getImplObjectMap().get(paras.getClassName()), paras.getParameters());
    }

    /**
     * apiProcess 不可能有2个Dto的接口,但是可能有多个基础类型 test(User user,Address add)//不会有这种情况,有也不支持 test(String
     * userName,String pwd)//会有
     *
     * @param paras
     * @return
     * @throws Exception
     */
    private Object apiProcess(HippoRequest paras) throws Exception {


        Method _method = HippoServiceCache.INSTANCE.getApiMethodMap().get(paras.getClassName() + "-" + paras.getMethodName());
        Class<?> aClass = HippoServiceCache.INSTANCE.getInterfaceMap().get(paras.getClassName());
        if (_method == null) {
            Method[] methods = aClass.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.getName().equals(paras.getMethodName())) {
                    continue;
                }
                _method = method;
                HippoServiceCache.INSTANCE.getApiMethodMap().put(paras.getClassName() + "-" + paras.getMethodName(), method);
                break;
            }
        }
        if (_method == null) {
            throw new NoSuchMethodException(paras.getClassName() + ":" + paras.getMethodName());
        }
        Object[] paramDto;

        Class<?>[] parameterTypes = _method.getParameterTypes();

        FastMethod serviceFastMethod = HippoServiceCache.INSTANCE.getImplClassMap().get(aClass.getName()).getMethod(_method.getName(), parameterTypes);

        String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(serviceFastMethod.getJavaMethod());

        Object[] objects = paras.getParameters();
        // 无参数
        if (parameterTypes.length == 0 || objects == null) {
            paramDto = null;
        }
        // 一个参数(是否是Dto)
        else if (parameterTypes.length == 1) {
            Class<?> parameterType = parameterTypes[0];
            paramDto = new Object[1];
            // 非自定义dto就是java原生类了
            if (CommonUtils.isJavaClass(parameterType)) {
                paramDto[0] = covert(getMap(objects).get(parameterNames[0]), parameterType);
            } else {
                paramDto[0] = FastJsonConvertUtils.jsonToJavaObject((String) objects[0], parameterType);
            }
        }
        // 多参
        else {
            paramDto = new Object[parameterNames.length];
            int index = 0;
            Map<String, Object> map = getMap(objects);
            for (String parameter : parameterNames) {
                paramDto[index] = covert(map.get(parameter), parameterTypes[index]);
                index++;
            }
        }
        // 拿到返回
        return FastJsonConvertUtils.cleanseToObject(serviceFastMethod.invoke(HippoServiceCache.INSTANCE.getImplObjectMap().get(aClass.getName()), paramDto));
    }

    /**
     * 常用类转换
     *
     * @param o             原始数据
     * @param parameterType 转换后的类型
     * @return 转换后的数据
     */
    private Object covert(Object o, Class<?> parameterType) {
        if (o == null) {
            return null;
        }
        String param = String.valueOf(o);
        if (parameterType == long.class || parameterType == Long.class) {
            return Long.valueOf(param);
        } else if (parameterType == int.class || parameterType == Integer.class) {
            return Integer.valueOf(param);
        } else if (parameterType == double.class || parameterType == Double.class) {
            return Double.valueOf(param);
        } else if (parameterType == float.class || parameterType == Float.class) {
            return Float.valueOf(param);
        } else if (parameterType == boolean.class || parameterType == Boolean.class) {
            return Boolean.valueOf(param);
        } else if (parameterType == BigDecimal.class) {
            return new BigDecimal(param);
        } else if (parameterType == String.class) {
            return param;
        }
        throw new HippoServiceException("数据转换失败,请检查接口参数,原始数据:" + param + ",转换类型:" + parameterType.getName());
    }

    private Map<String, Object> getMap(Object[] objects) {
        if (objects.length == 1) {
            return FastJsonConvertUtils.jsonToMap((String) objects[0]);
        } else {
            return new HashMap<>();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("netty server error", cause.fillInStackTrace());
        ctx.close();
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HippoRequest request) throws Exception {
        if (request != null && request.getRequestType() == HippoRequestEnum.PING.getType()) {
            // 单独的线程去执行心跳操作,业务请求不影响心跳
            HippoServerThreadPool.SINGLE.getPool().execute(() -> handle(ctx, request));
        } else {
            HippoServerThreadPool.FIXED.getPool().execute(() -> handle(ctx, request));
        }
    }

}
