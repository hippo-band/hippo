package com.github.hippo.goven.eureka;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.netflix.discovery.DiscoveryClient;

/**
 * EurekaGovernatedBaseService
 * 
 * @author wj
 *
 */
@Component
public final class EurekaGovernatedBaseService {
	private DiscoveryClient discoveryClient = null;

	@PreDestroy
	public void stop() {
		if (discoveryClient != null)
			discoveryClient.shutdown();
	}

	public DiscoveryClient getDiscoveryClient() {
		return discoveryClient;
	}

	public void setConfiguration(DiscoveryClient discoverClient) {
		this.discoveryClient = discoverClient;
	}

}
