package cloud.igoldenbeta.hippo.goven.serviceImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.DiscoveryManager;

import cloud.igoldenbeta.hippo.goven.eureka.EurekaClientConfigBean;
import cloud.igoldenbeta.hippo.goven.eureka.EurekaGovernatedBaseService;
import cloud.igoldenbeta.hippo.goven.eureka.EurekaInstanceConfigBean;
import cloud.igoldenbeta.hippo.govern.ServiceGovern;

@Component
public class ServiceGovenImpl implements ServiceGovern {
	private static final Logger logger = LoggerFactory.getLogger(ServiceGovenImpl.class);
	@Autowired
	private EurekaGovernatedBaseService baseService;

	@Value("${erueka.ipAddress:localhost}")
	private String ipAddress;
	@Value("${eureka.serviceUrls}")
	private String serviceUrl;
	@Value("${eureka.port:8761}")
	private int eurekaPort;
	@Value("${eureka.instance.preferIpAddress:true}")
	private boolean preferIpAddress;
	@Value("${eureka.instance.leaseRenewalIntervalInSeconds:30}")
	private int leaseRenewalIntervalInSeconds;
	@Value("${eureka.instance.leaseExpirationDurationInSeconds:90}")
	private int leaseExpirationDurationInSeconds;
	@Value("${eureka.client.registerWithEureka:true}")
	private boolean registerWithEureka;
	@Value("${eureka.client.preferSameZoneEureka:true}")
	private boolean preferSameZoneEureka;
	@Value("$eureka.client.region:us-east-1")
	private String region;
	@Value("${eureka.client.zone:defaultZone}")
	private String zone;

	@Override
	public String getServiceAddress(String arg0) {
		DiscoveryClient discoveryClient = baseService.getDiscoveryClient();
		if (discoveryClient == null) {
			discoveryClient = getClient();
			baseService.setConfiguration(discoveryClient);
		}
		try {
			InstanceInfo instanceInfo = discoveryClient.getNextServerFromEureka(arg0, false);
			if (instanceInfo != null) {
				return instanceInfo.getIPAddr() + ":" + instanceInfo.getPort();
			}
		} catch (Exception e) {
			logger.error("can not get an instance of service from euraka server", e);
			throw e;
		}
		String defaultAddress = "";
		try {
			defaultAddress = InetAddress.getLocalHost().getHostAddress() + ":8761";
		} catch (UnknownHostException e) {
			logger.error("return default address", e);
		}
		return defaultAddress;
	}

	private DiscoveryClient getClient() {
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

	@Override
	public int register(String arg0) {
		DiscoveryManager.getInstance().shutdownComponent();
		logger.info("------------正在注册------------" + arg0);
		EurekaInstanceConfigBean eureInstanceConfigBean = new EurekaInstanceConfigBean();
		EurekaClientConfigBean eureClientConfigBean = new EurekaClientConfigBean();
		String host = System.getenv("HOST");
		String port = System.getenv("PORT");
		if (StringUtils.isNotBlank(host)) {
			eureInstanceConfigBean.setIpAddress(host.trim());
		} else {
			if (!ipAddress.equals("localhost")) {
				eureInstanceConfigBean.setIpAddress(ipAddress);
			} else {
				try {
					eureInstanceConfigBean.setIpAddress(InetAddress.getLocalHost().getHostAddress());
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		}
		if (StringUtils.isNotBlank(port)) {
			eureInstanceConfigBean.setNonSecurePort(Integer.parseInt(port.trim()));
		} else {
			if (eurekaPort != 0) {
				eureInstanceConfigBean.setNonSecurePort(eurekaPort);
			} else {
				eureInstanceConfigBean.setNonSecurePort(7050);
			}
		}
		eureInstanceConfigBean.setAppname(arg0 == null ? "eureka" : arg0);
		eureInstanceConfigBean.setPreferIpAddress(preferIpAddress);
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
		DiscoveryManager.getInstance().initComponent(eureInstanceConfigBean, eureClientConfigBean);
		ApplicationInfoManager.getInstance().setInstanceStatus(InstanceStatus.UP);
		baseService.setConfiguration(DiscoveryManager.getInstance().getDiscoveryClient());
		return eureInstanceConfigBean.getNonSecurePort();
	}
}
