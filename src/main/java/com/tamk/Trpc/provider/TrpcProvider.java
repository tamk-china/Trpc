package com.tamk.Trpc.provider;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import com.tamk.Trpc.exception.TrpcException;
import com.tamk.Trpc.protocol.InvokeTO;
import com.tamk.Trpc.route.RouteManager;
import com.tamk.Trpc.utils.ProtocolUtils;

/**
 * @author kuanqiang.tkq
 */
public class TrpcProvider extends ChannelHandlerAdapter {
	private static Map<String, Object> invokers = new ConcurrentHashMap<String, Object>();
	private String interfaceName;
	private Object invoker;

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public Object getInvoker() {
		return invoker;
	}

	public void setInvoker(Object invoker) {
		this.invoker = invoker;
	}

	public void init() throws TrpcException {
		TrpcProxy.INSTANCE.init();
		if (StringUtils.isEmpty(interfaceName)) {
			throw new TrpcException("interface can not null");
		}
		if (null == invoker) {
			throw new TrpcException("invoker can not null");
		}

		RouteManager.INSTANCE.registerProvider(interfaceName);
		invokers.put(interfaceName, invoker);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException, TrpcException {
		InvokeTO param = (InvokeTO) msg;
		if (null == param) {
			throw new IllegalArgumentException();
		}

		Object invoker = invokers.get(param.getInterfaceName());
		if (null == invoker) {
			throw new TrpcException(String.format("none exist for [interfaceName = %s]", param.getInterfaceName()));
		}

		try {
			Method m = invoker.getClass().getMethod(param.getFunctionName(), param.getParamClasses());
			if (null == m) {
				throw new TrpcException(String.format("method not exist [functionName = %s] [funcParams = %s]", param.getFunctionName(), param.getParamClasses()));
			}

			Object result = m.invoke(invoker, param.getParams());
			if (!Serializable.class.isAssignableFrom(result.getClass())) {
				throw new TrpcException("result not serializable");
			}

			byte[] body = SerializationUtils.serialize((Serializable) result);
			ByteBuf resp = Unpooled.copiedBuffer(ProtocolUtils.getBytes(body.length), body);
			ctx.write(resp);
		} catch (NoSuchMethodException e) {
			throw new TrpcException(e);
		} catch (SecurityException e) {
			throw new TrpcException(e);
		} catch (IllegalAccessException e) {
			throw new TrpcException(e);
		} catch (IllegalArgumentException e) {
			throw new TrpcException(e);
		} catch (InvocationTargetException e) {
			throw new TrpcException(e);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}
}
