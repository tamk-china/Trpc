package com.tamk.Trpc.provider;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import org.apache.commons.lang3.SerializationUtils;

import com.tamk.Trpc.protocol.InvokeTO;
import com.tamk.Trpc.utils.ProtocolUtils;

public class TrpcDecoder extends ByteToMessageDecoder{
	public static final int HEAD_LENGTH = 4;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if(in.readableBytes() < HEAD_LENGTH){
			return;
		}
		
		in.markReaderIndex();
		int dataLength = ProtocolUtils.getUnsignedInt(in.readBytes(HEAD_LENGTH).array());
		if(dataLength < 0){
			ctx.close();
			return;
		}
		
		if(in.readableBytes() < dataLength){
			in.resetReaderIndex();
			return;
		}
		
		byte[] body = new byte[dataLength];
		in.readBytes(body);
		InvokeTO invokeTo = SerializationUtils.deserialize(body);
		out.add(invokeTo);
	}

}
