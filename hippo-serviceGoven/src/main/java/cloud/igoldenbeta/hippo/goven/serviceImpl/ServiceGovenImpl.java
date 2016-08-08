package cloud.igoldenbeta.hippo.goven.serviceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Override
	public String getServiceAddress(String arg0) {
		DiscoveryClient discoveryClient = baseService.getDiscoveryClient();
		if (discoveryClient == null) {
			register("eureka");
		}
		discoveryClient = baseService.getDiscoveryClient();
		try {
			InstanceInfo instanceInfo = discoveryClient.getNextServerFromEureka(arg0, false);
			if (instanceInfo != null) {
				return instanceInfo.getIPAddr() + ":" + instanceInfo.getPort();
			}
		} catch (Exception e) {
			logger.error("can not get an instance of service from euraka server", e);
			throw e;
		}
		return "127.0.0.1:7050";
	}

	@Override
	public int register(String arg0) {
		Properties properties = new Properties();
		EurekaInstanceConfigBean eureInstanceConfigBean = new EurekaInstanceConfigBean();
		EurekaClientConfigBean eureClientConfigBean = new EurekaClientConfigBean();
		InputStream in = null;
		try {
			  File file = new File(ServiceGovenImpl.class.getResource("/database.properties").getPath());
			  if(file.exists())
				  in = new FileInputStream(file);
			  else
			      in = new FileInputStream("conf/database.properties");
			  
			  properties.load(in);
			  String host = System.getenv("HOST");
			  String port = System.getenv("PORT");
			  if(StringUtils.isNotBlank(host)) {
				  eureInstanceConfigBean.setIpAddress(host.trim());
			  }else {
				  eureInstanceConfigBean.setIpAddress("127.0.0.1");
			  }
			  if(StringUtils.isNotBlank(port)) {
				  eureInstanceConfigBean.setNonSecurePort(Integer.parseInt(port.trim()));
			  }else {
				  eureInstanceConfigBean.setNonSecurePort(7050);
			  }
			  eureInstanceConfigBean.setAppname(arg0==null?"eureka":arg0);
			  eureInstanceConfigBean.setPreferIpAddress(true);
			  eureInstanceConfigBean.setVirtualHostName(eureInstanceConfigBean.getAppname());
			  eureClientConfigBean.setRegisterWithEureka(true);
			  eureClientConfigBean.setPreferSameZoneEureka(true);
			  Map<String,String> zones = new HashMap<>();
			  Map<String,String> serviceUrls = new HashMap<>();
			  zones.put(eureClientConfigBean.getRegion(), EurekaClientConfigBean.DEFAULT_ZONE);
			  serviceUrls.put(EurekaClientConfigBean.DEFAULT_ZONE,
						properties.getProperty("eureka.serviceUrls") == null ? EurekaClientConfigBean.DEFAULT_URL
								: properties.getProperty("eureka.serviceUrls"));
			  eureClientConfigBean.setAvailabilityZones(zones);
			  eureClientConfigBean.setServiceUrl(serviceUrls);
			  DiscoveryManager.getInstance().initComponent(eureInstanceConfigBean, eureClientConfigBean);
			  ApplicationInfoManager.getInstance().setInstanceStatus(InstanceStatus.UP);
			  baseService.setConfiguration(DiscoveryManager.getInstance().getDiscoveryClient());
		}catch(Exception e) {
			  logger.error("error to get service port",e);
		}finally{
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.error("error to close inpustream",e);
				}
			}
		}
		return eureInstanceConfigBean.getNonSecurePort();
	}

}
