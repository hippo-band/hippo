package com.github.hippo.goven.serviceImpl;

import com.github.hippo.exception.HippoServiceException;
import com.github.hippo.goven.eureka.EurekaClientConfigBean;
import com.github.hippo.goven.eureka.EurekaInstanceConfigBean;
import com.github.hippo.govern.ServiceGovern;
import com.github.hippo.govern.utils.ServiceGovernUtil;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.shared.Application;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务治理具体实现
 *
 * @author wj
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
    @Value("${eureka.client.region:china-shanghai-1}")
    private String region;
    @Value("${eureka.client.zone:defaultZone}")
    private String zone;


    private static ApplicationInfoManager findManager;

    private static synchronized ApplicationInfoManager initializeApplicationInfoManager(
            EurekaInstanceConfig instanceConfig) {
        if (findManager == null) {
            InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
            findManager = new ApplicationInfoManager(instanceConfig, instanceInfo);
        }
        return findManager;
    }

    private static ApplicationInfoManager regiestManager;

    private static synchronized ApplicationInfoManager initializeRegiestApplicationInfoManager(
            EurekaInstanceConfig instanceConfig) {
        if (regiestManager == null) {
            InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
            regiestManager = new ApplicationInfoManager(instanceConfig, instanceInfo);
        }
        return regiestManager;
    }

    private DiscoveryClient registerClient;

    private DiscoveryClient discoveryClient;

    @Override
    public String getServiceAddress(String arg0) {
        try {
            InstanceInfo instanceInfo = getDiscoverClient().getNextServerFromEureka(arg0, false);
            if (instanceInfo != null) {
                return instanceInfo.getIPAddr() + ":" + instanceInfo.getPort();
            }
        } catch (Exception e) {
            LOGGER.error("can not get an instance of service from euraka server " + arg0, e);
            discoveryShutdown();
        }
        return "";
    }

    @Override
    public String getServiceNameByClassName(String className) {
        if (StringUtils.isBlank(className)) {
            return null;
        }
        try {
            return getMetaMap().get(className);
        } catch (Exception e) {
            discoveryShutdown();
        }
        return null;
    }

    private Map<String, String> getMetaMap() {
        List<Application> registeredApplications = getDiscoverClient().getApplications().getRegisteredApplications();
        Map<String, String> metadata = new HashMap<>();
        for (Application app : registeredApplications) {
            for (InstanceInfo info :
                    app.getInstancesAsIsFromEureka()) {
                metadata.putAll(info.getMetadata());
            }
        }
        return metadata;
    }

    private DiscoveryClient getDiscoverClient() {
        synchronized (ServiceGovenImpl.class) {

            if (discoveryClient != null) {
                return discoveryClient;
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

            return new DiscoveryClient(initializeApplicationInfoManager(eureInstanceConfigBean),
                    eureClientConfigBean);
        }
    }

    @Override
    public int register(String arg0) {
        return register(arg0, null);
    }

    @Override
    public int register(String arg0, Map<String, String> metaMap) {

        synchronized (ServiceGovenImpl.class) {
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
            if (metaMap != null) {
                //check class name is exist?
                Map<String, String> metaMap1 = getMetaMap();
                for (String key : metaMap.keySet()) {
                    if (metaMap1.get(key) != null && !metaMap1.get(key).equals(arg0)) {
                        discoveryShutdown();
                        throw new HippoServiceException("服务注册异常,原因:[" + key + "]已被注册");
                    }
                }
                eureInstanceConfigBean.setMetadataMap(metaMap);
            }
            eureClientConfigBean.setRegisterWithEureka(registerWithEureka);
            eureClientConfigBean.setPreferSameZoneEureka(preferSameZoneEureka);
            Map<String, String> zones = new HashMap<>();
            Map<String, String> serviceUrls = new HashMap<>();
            zones.put(region, zone);
            serviceUrls.put(zone, serviceUrl);
            eureClientConfigBean.setAvailabilityZones(zones);
            eureClientConfigBean.setServiceUrl(serviceUrls);
            registerClient = new DiscoveryClient(
                    initializeRegiestApplicationInfoManager(eureInstanceConfigBean), eureClientConfigBean);
            registerClient.getApplicationInfoManager().setInstanceStatus(InstanceStatus.UP);
            int nonSecurePort = eureInstanceConfigBean.getNonSecurePort();
            LOGGER.info(arg0 + "------------注册成功------------port:" + nonSecurePort);
            return nonSecurePort;
        }
    }

    @Override
    public List<String> getServiceAddresses(String serviceName) {
        try {
            List<InstanceInfo> instancesByVipAddress =
                    getDiscoverClient().getInstancesByVipAddress(serviceName, false);
            return instancesByVipAddress.stream().map(i -> i.getIPAddr() + ":" + i.getPort())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            discoveryShutdown();
        }
        return Collections.emptyList();
    }

    @Override
    public void shutdown() {
        if (registerClient == null) {
            return;
        }
        synchronized (ServiceGovenImpl.class) {
            if (registerClient == null) {
                return;
            }
            registerClient.shutdown();
            registerClient = null;
        }

    }

    private void discoveryShutdown() {
        if (discoveryClient == null) {
            return;
        }
        synchronized (ServiceGovenImpl.class) {
            if (discoveryClient == null) {
                return;
            }
            discoveryClient.shutdown();
            discoveryClient = null;
        }

    }
}
