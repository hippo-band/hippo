package com.github.hippo.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.chain.ChainThreadLocal;
import com.github.hippo.enums.HippoRequestEnum;
import com.github.hippo.exception.HippoRequestTypeNotExistException;
import com.github.hippo.util.FastJsonConvertUtils;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * netty handler处理类
 * 
 * @author sl
 *
 */
@Sharable
public class HippoServerHandler extends SimpleChannelInboundHandler<HippoRequest> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HippoServerHandler.class);
  private static final ExecutorService pool =
      Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

  private void handle(ChannelHandlerContext ctx, HippoRequest request) {
    long start = System.currentTimeMillis();
    HippoResponse response = new HippoResponse();
    response.setChainId(request.getChainId());
    response.setChainOrder(request.getChainOrder());
    response.setServiceName(request.getServiceName());
    HippoRequestEnum hippoRequestEnum = HippoRequestEnum.getByType(request.getRequestType());
    if (hippoRequestEnum != HippoRequestEnum.PING) {
      LOGGER.info("hippo in param:{}", request);
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
        response.setResult("ping success");
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
      LOGGER.info("hippo out result:{},耗时:{}毫秒", response, System.currentTimeMillis() - start);
    }

    ctx.writeAndFlush(response);
  }

  private Object rpcProcess(HippoRequest paras) throws InvocationTargetException {
    Object serviceBean = HippoServiceCache.INSTANCE.getImplObjectMap().get(paras.getClassName());
    FastClass serviceFastClass = FastClass.create(serviceBean.getClass());
    FastMethod serviceFastMethod =
        serviceFastClass.getMethod(paras.getMethodName(), paras.getParameterTypes());
    return serviceFastMethod.invoke(serviceBean, paras.getParameters());
  }

  /**
   * apiProcess 不可能有2个Dto的接口,但是可能有多个基础类型 test(User user,Address add)//不会有这种情况,有也不支持 test(String
   * userName,String pwd)//会有
   * 
   * @param paras
   * @return
   * @throws Exception
   */
  private Object apiProcess(HippoRequest paras) throws Exception {/* 先不管重载 */

    Method[] methods =
        HippoServiceCache.INSTANCE.getInterfaceMap().get(paras.getClassName()).getDeclaredMethods();
    // 接口定义的method
    for (Method method : methods) {
      if (!method.getName().equals(paras.getMethodName())) {
        continue;
      }
      Object[] paramDto = null;

      Class<?>[] parameterTypes = method.getParameterTypes();

      Object serviceBean = HippoServiceCache.INSTANCE.getImplObjectMap().get(paras.getClassName());
      FastClass serviceFastClass = FastClass.create(serviceBean.getClass());
      FastMethod serviceFastMethod = serviceFastClass.getMethod(method.getName(), parameterTypes);

      LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
      String[] parameterNames = u.getParameterNames(serviceFastMethod.getJavaMethod());

      Object[] objects = paras.getParameters();

      if (parameterTypes.length == 0 || objects == null) {// 无参数
        paramDto = null;
      } else if (parameterTypes.length == 1) {// 一个参数(是否是Dto)
        Class<?> parameterType = parameterTypes[0];
        paramDto = new Object[1];
        // 非自定义dto就是java原生类了
        if (isJavaClass(parameterType)) {
          paramDto[0] = getMap(objects).get(parameterNames[0]);
        } else {
          paramDto[0] = FastJsonConvertUtils.jsonToJavaObject((String) objects[0], parameterType);
        }
      }
      // 多参
      else {
        if (parameterNames.length != 0) {
          paramDto = new Object[parameterNames.length];
          int index = 0;
          Map<String, Object> map = getMap(objects);
          for (String parameter : parameterNames) {
            paramDto[index] = map.get(parameter);
            index++;
          }
        }
      }
      // 拿到返回
      return FastJsonConvertUtils.cleanseToObject(serviceFastMethod.invoke(serviceBean, paramDto));
    }
    throw new NoSuchMethodException(paras.getMethodName());
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
    pool.execute(() -> handle(ctx, request));
  }

  private boolean isJavaClass(Class<?> clz) {
    return clz != null && clz.getClassLoader() == null;
  }
}
