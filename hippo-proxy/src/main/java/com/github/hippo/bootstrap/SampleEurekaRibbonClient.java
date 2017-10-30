package com.github.hippo.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.netflix.client.ClientFactory;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import com.netflix.loadbalancer.RandomRule;
import com.netflix.loadbalancer.Server;

@Component
public class SampleEurekaRibbonClient {

  public static int getAvailablePort() {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(0);
      return serverSocket.getLocalPort();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (serverSocket != null) {
        try {
          serverSocket.close();
        } catch (IOException e) {
          e.printStackTrace();
          serverSocket = null;
        }
      }
    }
    return 0;
  }

  public void sendRequestToServiceUsingEureka() throws UnknownHostException {
    DynamicServerListLoadBalancer lb =
        (DynamicServerListLoadBalancer) ClientFactory.getNamedLoadBalancer("myclient");
    // show all servers in the list
    List<Server> list = lb.getServerList(false);
    Iterator<Server> it = list.iterator();
    while (it.hasNext()) {
      Server server = it.next();
    }
    // use RandomRule 's RandomRule algorithm to get a random server from lb 's server list
    RandomRule randomRule = new RandomRule();
    Server randomAlgorithmServer = randomRule.choose(lb, null);
    // communicate with the server
    Socket s = new Socket();
    try {
      s.connect(
          new InetSocketAddress(randomAlgorithmServer.getHost(), randomAlgorithmServer.getPort()));
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      PrintStream out = new PrintStream(s.getOutputStream());
      out.println("Sample request " + new Date());
      String str = null;
      BufferedReader rd = new BufferedReader(new InputStreamReader(s.getInputStream()));
      str = rd.readLine();
      if (str != null) {
        System.out.println("str>>>>>" + str);
      }
      rd.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    this.unRegisterWithEureka();
  }


  public void unRegisterWithEureka() {
    DiscoveryManager.getInstance().shutdownComponent();
  }
}
