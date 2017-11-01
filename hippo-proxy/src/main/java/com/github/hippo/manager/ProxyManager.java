package com.github.hippo.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.hippo.govern.ServiceGovern;

@Component
public class ProxyManager {

  @Value("${service.name}")
  private String serviceName;


  @Autowired
  private ServiceGovern serviceGovern;

  public void register() {
    serviceGovern.register(serviceName);
  }

}
