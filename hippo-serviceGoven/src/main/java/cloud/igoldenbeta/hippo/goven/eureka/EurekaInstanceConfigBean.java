/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cloud.igoldenbeta.hippo.goven.eureka;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.appinfo.UniqueIdentifier;


public class EurekaInstanceConfigBean implements EurekaInstanceConfig {

  public String[] getHostInfo() {
    return hostInfo;
  }

  public void setHostInfo(String[] hostInfo) {
    this.hostInfo = hostInfo;
  }

  public boolean isPreferIpAddress() {
    return preferIpAddress;
  }

  public void setPreferIpAddress(boolean preferIpAddress) {
    this.preferIpAddress = preferIpAddress;
  }

  public InstanceStatus getInitialStatus() {
    return initialStatus;
  }

  public void setInitialStatus(InstanceStatus initialStatus) {
    this.initialStatus = initialStatus;
  }

  public void setAppname(String appname) {
    this.appname = appname;
  }

  public void setAppGroupName(String appGroupName) {
    this.appGroupName = appGroupName;
  }

  public void setInstanceEnabledOnit(boolean instanceEnabledOnit) {
    this.instanceEnabledOnit = instanceEnabledOnit;
  }

  public void setNonSecurePort(int nonSecurePort) {
    this.nonSecurePort = nonSecurePort;
  }

  public void setSecurePort(int securePort) {
    this.securePort = securePort;
  }

  public void setNonSecurePortEnabled(boolean nonSecurePortEnabled) {
    this.nonSecurePortEnabled = nonSecurePortEnabled;
  }

  public void setSecurePortEnabled(boolean securePortEnabled) {
    this.securePortEnabled = securePortEnabled;
  }

  public void setLeaseRenewalIntervalInSeconds(int leaseRenewalIntervalInSeconds) {
    this.leaseRenewalIntervalInSeconds = leaseRenewalIntervalInSeconds;
  }

  public void setLeaseExpirationDurationInSeconds(int leaseExpirationDurationInSeconds) {
    this.leaseExpirationDurationInSeconds = leaseExpirationDurationInSeconds;
  }

  public void setVirtualHostName(String virtualHostName) {
    this.virtualHostName = virtualHostName;
  }

  public void setSecureVirtualHostName(String secureVirtualHostName) {
    this.secureVirtualHostName = secureVirtualHostName;
  }

  public void setaSGName(String aSGName) {
    this.aSGName = aSGName;
  }

  public void setMetadataMap(Map<String, String> metadataMap) {
    this.metadataMap = metadataMap;
  }

  public void setDataCenterInfo(DataCenterInfo dataCenterInfo) {
    this.dataCenterInfo = dataCenterInfo;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public void setStatusPageUrlPath(String statusPageUrlPath) {
    this.statusPageUrlPath = statusPageUrlPath;
  }

  public void setStatusPageUrl(String statusPageUrl) {
    this.statusPageUrl = statusPageUrl;
  }

  public void setHomePageUrlPath(String homePageUrlPath) {
    this.homePageUrlPath = homePageUrlPath;
  }

  public void setHomePageUrl(String homePageUrl) {
    this.homePageUrl = homePageUrl;
  }

  public void setHealthCheckUrlPath(String healthCheckUrlPath) {
    this.healthCheckUrlPath = healthCheckUrlPath;
  }

  public void setHealthCheckUrl(String healthCheckUrl) {
    this.healthCheckUrl = healthCheckUrl;
  }

  public void setSecureHealthCheckUrl(String secureHealthCheckUrl) {
    this.secureHealthCheckUrl = secureHealthCheckUrl;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  private static final Log logger = LogFactory.getLog(EurekaInstanceConfigBean.class);

  private String[] hostInfo = initHostInfo();

  private String appname = "unknown";

  private String appGroupName;

  private boolean instanceEnabledOnit;

  private int nonSecurePort = 80;

  private int securePort = 443;

  private boolean nonSecurePortEnabled = true;

  private boolean securePortEnabled;

  private int leaseRenewalIntervalInSeconds = 30;

  private int leaseExpirationDurationInSeconds = 90;

  private String virtualHostName;

  private String secureVirtualHostName;

  private String aSGName;

  private Map<String, String> metadataMap = new HashMap<>();

  private DataCenterInfo dataCenterInfo = new IdentifyingDataCenterInfo();

  private String ipAddress = this.hostInfo[0];

  private String statusPageUrlPath = "/info";

  private String statusPageUrl;

  private String homePageUrlPath = "/";

  private String homePageUrl;

  private String healthCheckUrlPath = "/health";

  private String healthCheckUrl;

  private String secureHealthCheckUrl;

  private String namespace = "eureka";

  private String hostname = this.hostInfo[1];

  private boolean preferIpAddress = false;

  private InstanceStatus initialStatus = InstanceStatus.UP;

  public String getHostname() {
    return this.preferIpAddress ? this.ipAddress : this.hostname;
  }

  @Override
  public boolean getSecurePortEnabled() {
    return this.securePortEnabled;
  }

  private String[] initHostInfo() {
    String[] info = new String[2];
    try {
      info[0] = InetAddress.getLocalHost().getHostAddress();
      info[1] = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException ex) {
      logger.error("Cannot get host info", ex);
    }
    return info;
  }

  @Override
  public String getHostName(boolean refresh) {
    return this.preferIpAddress ? this.ipAddress : this.hostname;
  }

  private final class IdentifyingDataCenterInfo implements DataCenterInfo, UniqueIdentifier {

    private Name name = Name.MyOwn;

    @Override
    public String getId() {
      String instanceId = EurekaInstanceConfigBean.this.metadataMap.get("instanceId");
      if (instanceId != null) {
        String old = getHostname();
        String id = old.endsWith(instanceId) ? old : old + ":" + instanceId;
        return id;
      }
      return getHostname();
    }

    public Name getName() {
      return name;
    }

    @SuppressWarnings("unused")
    public void setName(Name name) {
      this.name = name;
    }

  }

  @Override
  public String getAppname() {
    return this.appname;
  }

  @Override
  public String getAppGroupName() {
    return this.appGroupName;
  }

  @Override
  public boolean isInstanceEnabledOnit() {
    return this.instanceEnabledOnit;
  }

  @Override
  public int getNonSecurePort() {
    return this.nonSecurePort;
  }

  @Override
  public int getSecurePort() {
    return this.securePort;
  }

  @Override
  public boolean isNonSecurePortEnabled() {
    return this.nonSecurePortEnabled;
  }

  @Override
  public int getLeaseRenewalIntervalInSeconds() {
    return this.leaseRenewalIntervalInSeconds;
  }

  @Override
  public int getLeaseExpirationDurationInSeconds() {
    return this.leaseExpirationDurationInSeconds;
  }

  @Override
  public String getVirtualHostName() {
    return this.virtualHostName;
  }

  @Override
  public String getSecureVirtualHostName() {
    return this.secureVirtualHostName;
  }

  @Override
  public String getASGName() {
    return this.aSGName;
  }

  @Override
  public Map<String, String> getMetadataMap() {
    return this.metadataMap;
  }

  @Override
  public DataCenterInfo getDataCenterInfo() {
    return this.dataCenterInfo;
  }

  @Override
  public String getIpAddress() {
    return this.ipAddress;
  }

  @Override
  public String getStatusPageUrlPath() {
    return this.statusPageUrlPath;
  }

  @Override
  public String getStatusPageUrl() {
    return this.statusPageUrl;
  }

  @Override
  public String getHomePageUrlPath() {
    return this.homePageUrlPath;
  }

  @Override
  public String getHomePageUrl() {
    return this.homePageUrl;
  }

  @Override
  public String getHealthCheckUrlPath() {
    return this.healthCheckUrlPath;
  }

  @Override
  public String getHealthCheckUrl() {
    return this.healthCheckUrl;
  }

  @Override
  public String getSecureHealthCheckUrl() {
    return this.secureHealthCheckUrl;
  }

  @Override
  public String getNamespace() {
    return this.namespace;
  }
}
