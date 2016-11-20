package com.github.hippo.netty;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hippo.bean.HippoDecoder;
import com.github.hippo.bean.HippoEncoder;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.client.HippoClientBootstrapMap;
import com.github.hippo.govern.ServiceGovern;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * hippo client Bootstrap
 * 
 * @author sl
 *
 */
public class HippoClientBootstrap {

  private static final Logger LOGGER = LoggerFactory.getLogger(HippoClientBootstrap.class);

  private String host;
  private int port;
  private int hippoReadTimeout;
  private boolean needTimeout;
  private String clientId;

  private Bootstrap bootstrap = new Bootstrap();
  private NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);
  // private Channel channel;
  private HippoRequestHandler handler;

  private ServiceGovern serviceGovern;

  public static HippoClientBootstrap getBootstrap(String clientId, int hippoReadTimeout,
      boolean needTimeout, ServiceGovern serviceGovern) throws Exception {

    if (!HippoClientBootstrapMap.containsKey(clientId)) {
      synchronized (HippoClientBootstrapMap.class) {
        if (!HippoClientBootstrapMap.containsKey(clientId)) {
          HippoClientBootstrap hippoClientBootstrap =
              new HippoClientBootstrap(clientId, hippoReadTimeout, needTimeout, serviceGovern);
          HippoClientBootstrapMap.put(clientId, hippoClientBootstrap);
        }
      }
    }
    return HippoClientBootstrapMap.get(clientId);
  }

  private HippoClientBootstrap(String clientId, int hippoReadTimeout, boolean needTimeout,
      ServiceGovern serviceGovern) throws Exception {
    this.clientId = clientId;
    this.hippoReadTimeout = hippoReadTimeout;
    this.needTimeout = needTimeout;
    this.serviceGovern = serviceGovern;
    init();
  }

  private void init() throws Exception {
    initHostAndPort();
    try {
      handler = new HippoRequestHandler(this.clientId, this.eventLoopGroup);
      bootstrap.group(eventLoopGroup);
      bootstrap.channel(NioSocketChannel.class);
      bootstrap.option(ChannelOption.TCP_NODELAY, true);
      bootstrap.handler(new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel channel) throws Exception {
          int readTimeout = 0;
          if (needTimeout) {
            readTimeout = hippoReadTimeout;
          }
          channel.pipeline().addLast(new IdleStateHandler(readTimeout, 30, 0, TimeUnit.SECONDS))
              .addLast(new HippoEncoder(HippoRequest.class))
              .addLast(new HippoDecoder(HippoResponse.class)).addLast(handler);
        }
      });
      bootstrap.connect(host, port).sync();
    } catch (Exception e) {
      LOGGER.error("send error", e);
      throw e;
    }
  }


  private void initHostAndPort() {
    // String serviceAddress = serviceGovern.getServiceAddress(clientId);
    // String[] split = serviceAddress.split(":");
    // this.host = split[0];
    // this.port = Integer.parseInt(split[1]);
    this.host = "127.0.0.1";
    this.port = 8888;
  }

  public void sendAsync(HippoRequest request) throws Exception {
    this.handler.sendAsync(request);
  }

  public HippoResponse getResult(String requestId) throws Exception {
    if (StringUtils.isBlank(requestId)) {
      HippoResponse hippoResponse = new HippoResponse();
      hippoResponse.setResult(null);
      hippoResponse.setError(true);
      hippoResponse.setThrowable(new IllegalArgumentException("requestId must be not null"));
      return hippoResponse;
    }
    while (true) {
      // 正常不会有问题,为了容错增加超时时间,后续做
      HippoResponse response = handler.getResponse(requestId);
      if (response != null) {
        return response;
      }
      // 服务器断了 就返回异常信息
      if (handler.isInActive()) {
        response = new HippoResponse();
        response.setResult(null);
        response.setError(true);
        response.setThrowable(new IllegalAccessError("hippo server error"));
        return response;
      }
    }
  }


  public static void main(String[] args) throws Exception {
    HippoClientBootstrap bootstrap =
        HippoClientBootstrap.getBootstrap("testClient111", 1, false, null);

    for (int i = 0; i < 3; i++) {
      new Thread(() -> {
        HippoRequest request = new HippoRequest();
        request.setClientId("testClient111");
        String requestId = "12345-" + new Random().nextInt(1000);
        request.setRequestId(requestId);
        try {
          bootstrap.sendAsync(request);
          // System.out.println(new Date().toLocaleString() + ">>222>>>"
          // + bootstrap.getResult(requestId).getResult());
          bootstrap.getResult(requestId);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).start();

    }

    HippoClientBootstrap bootstrap2 =
        HippoClientBootstrap.getBootstrap("testClient222", 1, false, null);

    for (int i = 0; i < 3; i++) {
      new Thread(() -> {
        HippoRequest request = new HippoRequest();
        request.setClientId("testClient222");
        String requestId = "abcde-" + new Random().nextInt(1000);
        request.setRequestId(requestId);
        try {
          bootstrap2.sendAsync(request);
          // System.out.println(new Date().toLocaleString() + ">>222>>>"
          // + bootstrap.getResult(requestId).getResult());
          bootstrap2.getResult(requestId);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).start();

    }
    // HippoClientBootstrap bootstrap2 = new HippoClientBootstrap("testClient222", 3, false, null);
    // for (int i = 0; i < 3; i++) {
    // HippoRequest request = new HippoRequest();
    // request.setClientId("testClient222");
    // request.setRequestId("abcde-" + i);
    // bootstrap2.sendAsync(request);
    // }
  }
}
