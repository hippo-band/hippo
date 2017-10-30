package com.github.hippo.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.github.hippo.manager.ProxyManager;

@SpringBootApplication
@ComponentScan({"com.github"})
public class SampleLogbackApplication implements CommandLineRunner {


  @Autowired
  private ProxyManager registerManager;

  @Autowired
  private SampleEurekaRibbonClient sampleEurekaRibbonClient;

  public static void main(String[] args) throws Exception {

    SpringApplication.run(SampleLogbackApplication.class, args);
  }

  @Override
  public void run(String... arg0) throws Exception {
    registerManager.register();
    sampleEurekaRibbonClient.sendRequestToServiceUsingEureka();
    Thread.currentThread().join();
  }
}
