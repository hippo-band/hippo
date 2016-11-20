package com.github.hippo.netty;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;
import com.github.hippo.enums.HippoRequestEnum;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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
  private static final ExecutorService pool = Executors.newFixedThreadPool(5);

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
      response.setRequestId("-99");
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

      System.out.println(new Date().toLocaleString() + ".." + i + ".." + request.getRequestId()
          + ".." + request.getClientId() + "..");
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
    pool.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        ctx.writeAndFlush(handle(ctx, request));
        return null;
      }
    });
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

  private static Lock lock = new ReentrantLock();
  private static Condition finish = lock.newCondition();

  public static void over() {
    try {
      lock.lock();
      finish.signal();
    } finally {
      lock.unlock();
    }
  }

  private static void start() {
    try {
      lock.lock();
      System.out.println("11~" + new Date().toLocaleString());
      // 设定一下超时时间，rpc服务器太久没有相应的话，就默认返回空吧。
      finish.await(10, TimeUnit.SECONDS);
      System.out.println("22~" + new Date().toLocaleString());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
    }
  }

}
