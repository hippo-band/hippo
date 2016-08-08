/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.igoldenbeta.hippo.goven.eureka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import com.netflix.discovery.EurekaClientConfig;

public class EurekaClientConfigBean implements EurekaClientConfig {

	public static final String DEFAULT_URL = "http://localhost:8761"
			+ "/eureka/";

	public static final String DEFAULT_ZONE = "defaultZone";

	private static final int MINUTES = 60;

	private boolean enabled = true;

	private int registryFetchIntervalSeconds = 30;

	private int instanceInfoReplicationIntervalSeconds = 30;

	private int initialInstanceInfoReplicationIntervalSeconds = 40;

	private int eurekaServiceUrlPollIntervalSeconds = 5 * MINUTES;

	private String proxyPort;

	private String proxyHost;

	private String proxyUserName;

	private String proxyPassword;

	private int eurekaServerReadTimeoutSeconds = 8;

	private int eurekaServerConnectTimeoutSeconds = 5;

	private String backupRegistryImpl;

	private int eurekaServerTotalConnections = 200;

	private int eurekaServerTotalConnectionsPerHost = 50;

	private String eurekaServerURLContext;

	private String eurekaServerPort;

	private String eurekaServerDNSName;

	private String region = "us-east-1";

	private int eurekaConnectionIdleTimeoutSeconds = 30;

	private String registryRefreshSingleVipAddress;

	private int heartbeatExecutorThreadPoolSize = 2;

	private int heartbeatExecutorExponentialBackOffBound = 10;

	private int cacheRefreshExecutorThreadPoolSize = 2;

	private int cacheRefreshExecutorExponentialBackOffBound = 10;

	private Map<String, String> serviceUrl = new HashMap<String, String>();
	{
		this.serviceUrl.put(DEFAULT_ZONE, DEFAULT_URL);
	}

	private boolean gZipContent = true;

	private boolean useDnsForFetchingServiceUrls = false;

	private boolean registerWithEureka = true;

	private boolean preferSameZoneEureka = true;

	private boolean logDeltaDiff;

	private boolean disableDelta;

	private String fetchRemoteRegionsRegistry;

	private Map<String, String> availabilityZones = new HashMap<String, String>();

	private boolean filterOnlyUpInstances = true;

	private boolean fetchRegistry = true;

	private String dollarReplacement = "_-";

	private String escapeCharReplacement = "__";

	@Override
	public boolean shouldGZipContent() {
		return this.gZipContent;
	}

	@Override
	public boolean shouldUseDnsForFetchingServiceUrls() {
		return this.useDnsForFetchingServiceUrls;
	}

	@Override
	public boolean shouldRegisterWithEureka() {
		return this.registerWithEureka;
	}

	@Override
	public boolean shouldPreferSameZoneEureka() {
		return this.preferSameZoneEureka;
	}

	@Override
	public boolean shouldLogDeltaDiff() {
		return this.logDeltaDiff;
	}

	@Override
	public boolean shouldDisableDelta() {
		return this.disableDelta;
	}

	@Override
	public String fetchRegistryForRemoteRegions() {
		return this.fetchRemoteRegionsRegistry;
	}

	@Override
	public String[] getAvailabilityZones(String region) {
		String value = this.availabilityZones.get(region);
		if (value == null) {
			value = DEFAULT_ZONE;
		}
		return value.split(",");
	}

	@Override
	public List<String> getEurekaServerServiceUrls(String myZone) {
		String serviceUrls = this.serviceUrl.get(myZone);
		if (serviceUrls == null || serviceUrls.isEmpty()) {
			serviceUrls = this.serviceUrl.get(DEFAULT_ZONE);
		}
		if (serviceUrls != null) {
			return Arrays.asList(serviceUrls.split(","));
		}

		return new ArrayList<>();
	}

	@Override
	public boolean shouldFilterOnlyUpInstances() {
		return this.filterOnlyUpInstances;
	}

	@Override
	public boolean shouldFetchRegistry() {
		return this.fetchRegistry;
	}

	@Override
	public int getRegistryFetchIntervalSeconds() {
		return this.registryFetchIntervalSeconds;
	}

	@Override
	public int getInstanceInfoReplicationIntervalSeconds() {
		return this.instanceInfoReplicationIntervalSeconds;
	}

	@Override
	public int getInitialInstanceInfoReplicationIntervalSeconds() {
		return this.initialInstanceInfoReplicationIntervalSeconds;
	}

	@Override
	public int getEurekaServiceUrlPollIntervalSeconds() {
		return this.eurekaServiceUrlPollIntervalSeconds;
	}

	@Override
	public String getProxyHost() {
		return this.proxyHost;
	}

	@Override
	public String getProxyPort() {
		return this.proxyPort;
	}

	@Override
	public String getProxyUserName() {
		return this.proxyUserName;
	}

	@Override
	public String getProxyPassword() {
		return this.proxyPassword;
	}

	@Override
	public int getEurekaServerReadTimeoutSeconds() {
		return this.eurekaServerReadTimeoutSeconds;
	}

	@Override
	public int getEurekaServerConnectTimeoutSeconds() {
		return this.eurekaServerConnectTimeoutSeconds;
	}

	@Override
	public String getBackupRegistryImpl() {
		return this.backupRegistryImpl;
	}

	@Override
	public int getEurekaServerTotalConnections() {
		return this.eurekaServerTotalConnections;
	}

	@Override
	public int getEurekaServerTotalConnectionsPerHost() {
		return this.eurekaServerTotalConnectionsPerHost;
	}

	@Override
	public String getEurekaServerURLContext() {
		return this.eurekaServerURLContext;
	}

	@Override
	public String getEurekaServerPort() {
		return this.eurekaServerPort;
	}

	@Override
	public String getEurekaServerDNSName() {
		return this.eurekaServerDNSName;
	}

	@Override
	public String getRegion() {
		return this.region;
	}

	@Override
	public int getEurekaConnectionIdleTimeoutSeconds() {
		return this.eurekaConnectionIdleTimeoutSeconds;
	}

	@Override
	public String getRegistryRefreshSingleVipAddress() {
		return this.registryRefreshSingleVipAddress;
	}

	@Override
	public int getHeartbeatExecutorThreadPoolSize() {
		return this.heartbeatExecutorThreadPoolSize;
	}

	@Override
	public int getHeartbeatExecutorExponentialBackOffBound() {
		return this.heartbeatExecutorExponentialBackOffBound;
	}

	@Override
	public int getCacheRefreshExecutorThreadPoolSize() {
		return this.cacheRefreshExecutorThreadPoolSize;
	}

	@Override
	public int getCacheRefreshExecutorExponentialBackOffBound() {
		return this.cacheRefreshExecutorExponentialBackOffBound;
	}

	@Override
	public String getDollarReplacement() {
		return this.dollarReplacement;
	}

	@Override
	public String getEscapeCharReplacement() {
		return this.escapeCharReplacement;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Map<String, String> getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(Map<String, String> serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public boolean isgZipContent() {
		return gZipContent;
	}

	public void setgZipContent(boolean gZipContent) {
		this.gZipContent = gZipContent;
	}

	public boolean isUseDnsForFetchingServiceUrls() {
		return useDnsForFetchingServiceUrls;
	}

	public void setUseDnsForFetchingServiceUrls(boolean useDnsForFetchingServiceUrls) {
		this.useDnsForFetchingServiceUrls = useDnsForFetchingServiceUrls;
	}

	public boolean isRegisterWithEureka() {
		return registerWithEureka;
	}

	public void setRegisterWithEureka(boolean registerWithEureka) {
		this.registerWithEureka = registerWithEureka;
	}

	public boolean isPreferSameZoneEureka() {
		return preferSameZoneEureka;
	}

	public void setPreferSameZoneEureka(boolean preferSameZoneEureka) {
		this.preferSameZoneEureka = preferSameZoneEureka;
	}

	public boolean isLogDeltaDiff() {
		return logDeltaDiff;
	}

	public void setLogDeltaDiff(boolean logDeltaDiff) {
		this.logDeltaDiff = logDeltaDiff;
	}

	public boolean isDisableDelta() {
		return disableDelta;
	}

	public void setDisableDelta(boolean disableDelta) {
		this.disableDelta = disableDelta;
	}

	public String getFetchRemoteRegionsRegistry() {
		return fetchRemoteRegionsRegistry;
	}

	public void setFetchRemoteRegionsRegistry(String fetchRemoteRegionsRegistry) {
		this.fetchRemoteRegionsRegistry = fetchRemoteRegionsRegistry;
	}

	public Map<String, String> getAvailabilityZones() {
		return availabilityZones;
	}

	public void setAvailabilityZones(Map<String, String> availabilityZones) {
		this.availabilityZones = availabilityZones;
	}

	public boolean isFilterOnlyUpInstances() {
		return filterOnlyUpInstances;
	}

	public void setFilterOnlyUpInstances(boolean filterOnlyUpInstances) {
		this.filterOnlyUpInstances = filterOnlyUpInstances;
	}

	public boolean isFetchRegistry() {
		return fetchRegistry;
	}

	public void setFetchRegistry(boolean fetchRegistry) {
		this.fetchRegistry = fetchRegistry;
	}

	public static int getMinutes() {
		return MINUTES;
	}

	public void setRegistryFetchIntervalSeconds(int registryFetchIntervalSeconds) {
		this.registryFetchIntervalSeconds = registryFetchIntervalSeconds;
	}

	public void setInstanceInfoReplicationIntervalSeconds(int instanceInfoReplicationIntervalSeconds) {
		this.instanceInfoReplicationIntervalSeconds = instanceInfoReplicationIntervalSeconds;
	}

	public void setInitialInstanceInfoReplicationIntervalSeconds(int initialInstanceInfoReplicationIntervalSeconds) {
		this.initialInstanceInfoReplicationIntervalSeconds = initialInstanceInfoReplicationIntervalSeconds;
	}

	public void setEurekaServiceUrlPollIntervalSeconds(int eurekaServiceUrlPollIntervalSeconds) {
		this.eurekaServiceUrlPollIntervalSeconds = eurekaServiceUrlPollIntervalSeconds;
	}

	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public void setProxyUserName(String proxyUserName) {
		this.proxyUserName = proxyUserName;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public void setEurekaServerReadTimeoutSeconds(int eurekaServerReadTimeoutSeconds) {
		this.eurekaServerReadTimeoutSeconds = eurekaServerReadTimeoutSeconds;
	}

	public void setEurekaServerConnectTimeoutSeconds(int eurekaServerConnectTimeoutSeconds) {
		this.eurekaServerConnectTimeoutSeconds = eurekaServerConnectTimeoutSeconds;
	}

	public void setBackupRegistryImpl(String backupRegistryImpl) {
		this.backupRegistryImpl = backupRegistryImpl;
	}

	public void setEurekaServerTotalConnections(int eurekaServerTotalConnections) {
		this.eurekaServerTotalConnections = eurekaServerTotalConnections;
	}

	public void setEurekaServerTotalConnectionsPerHost(int eurekaServerTotalConnectionsPerHost) {
		this.eurekaServerTotalConnectionsPerHost = eurekaServerTotalConnectionsPerHost;
	}

	public void setEurekaServerURLContext(String eurekaServerURLContext) {
		this.eurekaServerURLContext = eurekaServerURLContext;
	}

	public void setEurekaServerPort(String eurekaServerPort) {
		this.eurekaServerPort = eurekaServerPort;
	}

	public void setEurekaServerDNSName(String eurekaServerDNSName) {
		this.eurekaServerDNSName = eurekaServerDNSName;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setEurekaConnectionIdleTimeoutSeconds(int eurekaConnectionIdleTimeoutSeconds) {
		this.eurekaConnectionIdleTimeoutSeconds = eurekaConnectionIdleTimeoutSeconds;
	}

	public void setRegistryRefreshSingleVipAddress(String registryRefreshSingleVipAddress) {
		this.registryRefreshSingleVipAddress = registryRefreshSingleVipAddress;
	}

	public void setHeartbeatExecutorThreadPoolSize(int heartbeatExecutorThreadPoolSize) {
		this.heartbeatExecutorThreadPoolSize = heartbeatExecutorThreadPoolSize;
	}

	public void setHeartbeatExecutorExponentialBackOffBound(int heartbeatExecutorExponentialBackOffBound) {
		this.heartbeatExecutorExponentialBackOffBound = heartbeatExecutorExponentialBackOffBound;
	}

	public void setCacheRefreshExecutorThreadPoolSize(int cacheRefreshExecutorThreadPoolSize) {
		this.cacheRefreshExecutorThreadPoolSize = cacheRefreshExecutorThreadPoolSize;
	}

	public void setCacheRefreshExecutorExponentialBackOffBound(int cacheRefreshExecutorExponentialBackOffBound) {
		this.cacheRefreshExecutorExponentialBackOffBound = cacheRefreshExecutorExponentialBackOffBound;
	}

	public void setDollarReplacement(String dollarReplacement) {
		this.dollarReplacement = dollarReplacement;
	}

	public void setEscapeCharReplacement(String escapeCharReplacement) {
		this.escapeCharReplacement = escapeCharReplacement;
	}
    
}
