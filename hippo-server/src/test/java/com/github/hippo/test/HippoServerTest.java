package com.github.hippo.test;

import com.github.hippo.bean.HippoDecoder;
import com.github.hippo.bean.HippoEncoder;
import com.github.hippo.bean.HippoRequest;
import com.github.hippo.bean.HippoResponse;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class HippoServerTest {
  public static void main(String[] args) {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel channel) throws Exception {
              channel.pipeline().addLast(new HippoDecoder(HippoRequest.class))
                  .addLast(new HippoEncoder(HippoResponse.class))
                  .addLast(new HippoServerHandlerTest());
            }
          }).option(ChannelOption.SO_BACKLOG, 128).option(ChannelOption.TCP_NODELAY, true);

      ChannelFuture future = bootstrap.bind(8888).sync();
      future.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }
}
