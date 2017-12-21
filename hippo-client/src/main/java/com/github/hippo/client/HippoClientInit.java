package com.github.hippo.client;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

import com.github.hippo.annotation.HippoClient;
import com.github.hippo.annotation.HippoService;
import com.github.hippo.govern.ServiceGovern;
import com.github.hippo.netty.HippoClientBootstrap;
import com.github.hippo.netty.HippoClientBootstrapMap;

/**
 * 初始化有@RpcConsumer注解的类
 * 
 * @author sl
 *
 */
@Configuration
@Order(1)
public class HippoClientInit implements ApplicationContextAware, InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(HippoClientInit.class);

  private static ApplicationContext applicationContext;

  private Map<String, Object> rpcConsumerMap = new HashMap<>();
  @Autowired
  private HippoProxy hippoProxy;

  @Autowired
  private ServiceGovern serviceGovern;

  private Set<String> serviceNames = new HashSet<>();



  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    HippoClientInit.applicationContext = applicationContext;
  }

  @Bean
  public BeanPostProcessor beanPostProcessor() {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(Object bean, String beanName)
          throws BeansException {
        Class<?> objClz = bean.getClass();
        if (AopUtils.isAopProxy(bean)) {
          objClz = AopUtils.getTargetClass(bean);
        }
        for (Field field : objClz.getDeclaredFields()) {
          HippoClient hippoClient = field.getAnnotation(HippoClient.class);
          if (hippoClient != null) {
            Class<?> type = field.getType();
            String key = type.getCanonicalName();
            if (!rpcConsumerMap.containsKey(key)) {
              rpcConsumerMap.put(key, hippoProxy.create(type, hippoClient));
            }
            try {
              serviceNames.add(type.getAnnotation(HippoService.class).serviceName());
              field.setAccessible(true);
              field.set(bean, rpcConsumerMap.get(key));
              field.setAccessible(false);
            } catch (Exception e) {
              throw new BeanCreationException(beanName, e);
            }
          }
        }
        return bean;
      }


      @Override
      public Object postProcessAfterInitialization(Object bean, String beanName)
          throws BeansException {
        return bean;
      }
    };

  }

  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(1);
    newScheduledThreadPool.scheduleAtFixedRate(() -> {
      if (CollectionUtils.isEmpty(serviceNames)) {
        return;
      }
      serviceNames.forEach(this::conntectionProcess);
    }, 120, 15, TimeUnit.SECONDS);
  }

  private void conntectionProcess(String serviceName) {
    List<String> serviceAddresses = null;
    try {
      serviceAddresses = serviceGovern.getServiceAddresses(serviceName);
    } catch (Exception e) {
      LOGGER.error("getServiceAddresses error:[" + serviceName + "],每10秒会重试", e);
      return;
    }
    if (CollectionUtils.isEmpty(serviceAddresses)) {
      return;
    }
    for (String serviceAddress : serviceAddresses) {
      String[] split = serviceAddress.split(":");
      String host = split[0];
      int port = Integer.parseInt(split[1]);
      if (StringUtils.isBlank(host) || port <= 0 || port > 65532) {
        LOGGER.warn("[%s]服务参数异常.host=%s,port=%s", serviceName, host, port);
        continue;
      }
      createHippoHandler(serviceName, host, port);

    }
  }

  static void createHippoHandler(String serviceName, String host, int port) {
    synchronized (HippoClientInit.class) {
      if (checkServiceExist(serviceName, host, port)) {
        return;
      }
      try {
        HippoClientBootstrap bootstrap = new HippoClientBootstrap(serviceName, host, port);
        HippoClientBootstrapMap.put(serviceName, host, port, bootstrap);
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
      }
    }

  }

  private static boolean checkServiceExist(String serviceName, String host, int port) {
    return HippoClientBootstrapMap.containsSubKey(serviceName, host + ":" + port);
  }

}
