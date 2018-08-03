package com.github.hippo.bean;

import com.github.hippo.util.SerializationUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * netty msg encodeer
 *
 * @author sl
 */
public class HippoEncoder extends MessageToByteEncoder<Object> {

    private Class<?> genericClass;

    public HippoEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) {
        if (genericClass.isInstance(in)) {
            byte[] data = SerializationUtils.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
