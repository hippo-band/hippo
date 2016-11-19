package com.github.hippo.netty;

import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.enums.HippoRequestEnum;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

/**
 * netty handler处理类
 * 
 * @author sl
 *
 */
public class HippoServerHandlerTest extends SimpleChannelInboundHandler<HippoRequest> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HippoServerHandlerTest.class);

  private Object handle(ChannelHandlerContext ctx, HippoRequest request) {
    String clientId = request.getClientId();
    if (StringUtils.isNotBlank(clientId) && !HippoChannelMap.containsKey(clientId)) {
      HippoChannelMap.put(clientId, (SocketChannel) ctx.channel());
    }
    Integer i = new Random().nextInt(10000);
    HippoResponse response = new HippoResponse();
    response.setRequestId(request.getRequestId());
    // client ping
    if (request.getRequestType() == HippoRequestEnum.PING.getType()) {
      response.setResult("ping success");
      HippoChannelMap.get(clientId).writeAndFlush(response);
      System.out
          .println("ping:" + Thread.currentThread().getId() + ".." + request.getClientId() + "..");
    } else {
      response.setError(true);
      try {
        Thread.sleep(3 * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      System.out.println(i + ".." + request.getRequestId() + ".." + request.getClientId() + "..");
    }
    response.setResult(i);
    return response;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOGGER.error("netty server error", cause.fillInStackTrace());
    ctx.close();
  }


  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HippoRequest request) throws Exception {
    ctx.writeAndFlush(handle(ctx, request));
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    HippoChannelMap.remove((SocketChannel) ctx.channel());
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
  }


}
