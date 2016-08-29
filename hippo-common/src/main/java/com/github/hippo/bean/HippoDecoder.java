package com.github.hippo.bean;

import java.util.List;

import com.github.hippo.util.SerializationUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class HippoDecoder extends ByteToMessageDecoder {
  private Class<?> genericClass;

  public HippoDecoder(Class<?> genericClass) {
    this.genericClass = genericClass;
  }

  @Override
  public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (in.readableBytes() < 4) {
      return;
    }
    in.markReaderIndex();
    int dataLength = in.readInt();
    if (dataLength < 0) {
      ctx.close();
    }
    if (in.readableBytes() < dataLength) {
      in.resetReaderIndex();
      return;
    }
    byte[] data = new byte[dataLength];
    in.readBytes(data);
    out.add(SerializationUtils.deserialize(data, genericClass));
  }
}
