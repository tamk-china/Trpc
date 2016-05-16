package com.tamk.Trpc.provider;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.Serializable;

import com.tamk.Trpc.utils.SerializationUtils;

public class TrpcEncoder extends MessageToByteEncoder<Serializable> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Serializable msg, ByteBuf out) throws Exception {
		byte[] body = SerializationUtils.serialize(msg);
		out.writeInt(body.length);
		out.writeBytes(body);
	}

}
