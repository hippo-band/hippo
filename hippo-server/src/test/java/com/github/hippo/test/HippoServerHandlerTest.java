package com.github.hippo.test;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.enums.HippoRequestEnum;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * netty handler处理类
 * 
 * @author sl
 *
 */
public class HippoServerHandlerTest extends SimpleChannelInboundHandler<HippoRequest> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HippoServerHandlerTest.class);
  private static final ExecutorService pool = Executors.newCachedThreadPool();

  private void handle(ChannelHandlerContext ctx, HippoRequest request) {
    Integer i = new Random().nextInt(10000);
    HippoResponse response = new HippoResponse();
    response.setRequestId(request.getRequestId());
    // client ping
    if (request.getRequestType() == HippoRequestEnum.PING.getType()) {
      response.setResult("ping success");
      response.setRequestId("-99");
      // HippoChannelMap.get(clientId).writeAndFlush(response);
    } else {
      response.setError(true);
      try {
        Thread.sleep(3 * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println(new Date().toLocaleString() + ".." + i + ".." + request.getRequestId() + ".."
        + request.getClientId() + "..");
    response.setResult(i);
    ctx.writeAndFlush(response);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOGGER.error("netty server error", cause.fillInStackTrace());
    ctx.close();
  }


  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HippoRequest request) throws Exception {
    pool.submit(() -> {
      handle(ctx, request);
    });
  }
}
