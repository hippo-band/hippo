package com.github.hippo.goven.serviceImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.hippo.goven.eureka.EurekaClientConfigBean;
import com.github.hippo.goven.eureka.EurekaInstanceConfigBean;
import com.github.hippo.govern.ServiceGovern;
import com.github.hippo.govern.utils.ServiceGovernUtil;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;

/**
 * 服务治理具体实现
 * 
 * @author wj
 *
 */
@Component
public class ServiceGovenImpl implements ServiceGovern {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceGovenImpl.class);

  @Value("${eureka.ipAddress:localhost}")
  private String ipAddress;
  @Value("${eureka.serviceUrl}")
  private String serviceUrl;
  @Value("${eureka.port:0}")
  private int eurekaPort;
  @Value("${eureka.instance.preferIpAddress:true}")
  private boolean preferIpAddress;
  @Value("${eureka.instance.leaseRenewalIntervalInSeconds:10}")
  private int leaseRenewalIntervalInSeconds;
  @Value("${eureka.instance.leaseExpirationDurationInSeconds:25}")
  private int leaseExpirationDurationInSeconds;
  @Value("${eureka.client.registerWithEureka:true}")
  private boolean registerWithEureka;
  @Value("${eureka.client.preferSameZoneEureka:true}")
  private boolean preferSameZoneEureka;
  @Value("${eureka.client.region:us-east-1}")
  private String region;
  @Value("${eureka.client.zone:defaultZone}")
  private String zone;

  @Override
  public String getServiceAddress(String arg0) {
    DiscoveryClient discoveryClient = getClient();

    try {
      InstanceInfo instanceInfo = discoveryClient.getNextServerFromEureka(arg0, false);
      if (instanceInfo != null) {
        return instanceInfo.getIPAddr() + ":" + instanceInfo.getPort();
      }
    } catch (Exception e) {
      LOGGER.error("can not get an instance of service from euraka server " + arg0, e);
      throw e;
    }
    String defaultAddress = "";
    try {
      defaultAddress = InetAddress.getLocalHost().getHostAddress() + ":8761";
    } catch (UnknownHostException e) {
      LOGGER.error("return default address", e);
    }
    return defaultAddress;
  }

  private DiscoveryClient getClient() {
    synchronized (ServiceGovenImpl.class) {
      if (DiscoveryManager.getInstance().getDiscoveryClient() != null) {
        return DiscoveryManager.getInstance().getDiscoveryClient();
      }
      EurekaClientConfigBean eureClientConfigBean = new EurekaClientConfigBean();
      EurekaInstanceConfigBean eureInstanceConfigBean = new EurekaInstanceConfigBean();
      eureClientConfigBean.setRegisterWithEureka(false);
      eureClientConfigBean.setPreferSameZoneEureka(true);
      Map<String, String> zones = new HashMap<>();
      Map<String, String> serviceUrls = new HashMap<>();
      zones.put(eureClientConfigBean.getRegion(), EurekaClientConfigBean.DEFAULT_ZONE);
      serviceUrls.put(EurekaClientConfigBean.DEFAULT_ZONE,
          StringUtils.isBlank(serviceUrl) ? EurekaClientConfigBean.DEFAULT_URL : serviceUrl);
      eureClientConfigBean.setAvailabilityZones(zones);
      eureClientConfigBean.setServiceUrl(serviceUrls);
      DiscoveryManager.getInstance().initComponent(eureInstanceConfigBean, eureClientConfigBean);
      return DiscoveryManager.getInstance().getDiscoveryClient();
    }
  }

  @Override
  public int register(String arg0) {
    synchronized (ServiceGovenImpl.class) {
      DiscoveryManager.getInstance().shutdownComponent();
      LOGGER.info("------------正在注册------------" + arg0);
      EurekaInstanceConfigBean eureInstanceConfigBean = new EurekaInstanceConfigBean();
      EurekaClientConfigBean eureClientConfigBean = new EurekaClientConfigBean();
      String host = System.getenv("HOST");
      String port = System.getenv("PORT");
      if (StringUtils.isNotBlank(host)) {
        eureInstanceConfigBean.setIpAddress(host.trim());
      } else {
        if (!"localhost".equals(ipAddress)) {
          eureInstanceConfigBean.setIpAddress(ipAddress);
        } else {
          try {
            eureInstanceConfigBean.setIpAddress(InetAddress.getLocalHost().getHostAddress());
          } catch (UnknownHostException e) {
            LOGGER.error("InetAddress.getLocalHost() error", e);
          }
        }
      }
      if (StringUtils.isNotBlank(port)) {
        eureInstanceConfigBean.setNonSecurePort(Integer.parseInt(port.trim()));
      } else {
        if (eurekaPort != 0) {
          eureInstanceConfigBean.setNonSecurePort(eurekaPort);
        } else {
          eureInstanceConfigBean.setNonSecurePort(ServiceGovernUtil.getAvailablePort());
        }
      }
      eureInstanceConfigBean.setAppname(arg0 == null ? "eureka" : arg0);
      eureInstanceConfigBean.setPreferIpAddress(preferIpAddress);
      eureInstanceConfigBean.setLeaseRenewalIntervalInSeconds(leaseRenewalIntervalInSeconds);
      eureInstanceConfigBean.setLeaseExpirationDurationInSeconds(leaseExpirationDurationInSeconds);
      eureInstanceConfigBean.setVirtualHostName(eureInstanceConfigBean.getAppname());
      eureClientConfigBean.setRegisterWithEureka(registerWithEureka);
      eureClientConfigBean.setPreferSameZoneEureka(preferSameZoneEureka);
      Map<String, String> zones = new HashMap<>();
      Map<String, String> serviceUrls = new HashMap<>();
      zones.put(region, zone);
      serviceUrls.put(zone, serviceUrl);
      eureClientConfigBean.setAvailabilityZones(zones);
      eureClientConfigBean.setServiceUrl(serviceUrls);
      ApplicationInfoManager.getInstance().initComponent(eureInstanceConfigBean);
      DiscoveryManager.getInstance().shutdownComponent();
      DiscoveryManager.getInstance().initComponent(eureInstanceConfigBean, eureClientConfigBean);
      ApplicationInfoManager.getInstance().setInstanceStatus(InstanceStatus.UP);
      return eureInstanceConfigBean.getNonSecurePort();
    }
  }

  @Override
  public List<String> getServiceAddresses(String serviceName) {
    DiscoveryClient discoveryClient = getClient();
    List<InstanceInfo> instancesByVipAddress =
        discoveryClient.getInstancesByVipAddress(serviceName, false);
    return instancesByVipAddress.stream().map(i -> i.getIPAddr() + ":" + i.getPort())
        .collect(Collectors.toList());
  }
}
