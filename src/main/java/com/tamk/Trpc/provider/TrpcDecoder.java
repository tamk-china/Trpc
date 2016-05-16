package com.tamk.Trpc.provider;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import com.tamk.Trpc.protocol.InvokeTO;
import com.tamk.Trpc.utils.SerializationUtils;

public class TrpcDecoder extends ByteToMessageDecoder{
	public static final int HEAD_LENGTH = 4;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if(in.readableBytes() < HEAD_LENGTH){
			return;
		}
		
		in.markReaderIndex();
		int dataLength = in.readInt();
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
