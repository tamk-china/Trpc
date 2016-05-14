package com.tamk.Trpc.consumer;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.Socket;

import org.apache.commons.io.IOUtils;

import com.tamk.Trpc.balence.RoundRobinBalencer;
import com.tamk.Trpc.exception.TrpcException;
import com.tamk.Trpc.pool.ConnectorPool;
import com.tamk.Trpc.protocol.InvokeTO;
import com.tamk.Trpc.route.IpCache;
import com.tamk.Trpc.route.RouteManager;
import com.tamk.Trpc.utils.SerializationUtils;

/**
 * @author kuanqiang.tkq
 */
public class TrpcConsumer implements InvocationHandler {
	private String interfaceName;
	private Object proxy;

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public Object getProxy() {
		return proxy;
	}

	public void setProxy(Object proxy) {
		this.proxy = proxy;
	}

	public void init() throws TrpcException {
		RouteManager.INSTANCE.registerConsumer(interfaceName);
	}

	@Override
	public Object invoke(Object arg, Method method, Object[] params) throws Throwable {
		InvokeTO invokeTO = new InvokeTO();
		invokeTO.setInterfaceName(interfaceName);
		invokeTO.setFunctionName(method.getName());
		invokeTO.setParamClasses(method.getParameterTypes());
		invokeTO.setParams(params);

		Socket socket = ConnectorPool.INSTANCE.getSocket(interfaceName, RoundRobinBalencer.INSTANCE.getNextIp(interfaceName, IpCache.INSTANCE.get(interfaceName)));
		
		OutputStream socketOS = socket.getOutputStream();
		if (null == socketOS) {
			throw new TrpcException(String.format("outputStream of socket not exist [interfaceName = %s]", interfaceName));
		}
		socketOS.write(SerializationUtils.serialize(invokeTO));
		socketOS.flush();

		InputStream socketIS = socket.getInputStream();
		if (null == socketIS) {
			throw new TrpcException(String.format("inputStream of socket not exist [interfaceName = %s]", interfaceName));
		}
		
		//byte[] result = new byte[]
		//return SerializationUtils.deserialize()
		
		return null;
	}

	private int getOffset(byte[] bytes) {
		if (null == bytes || bytes.length < 4) {
			throw new IllegalArgumentException();
		}

		return bytes[0] << 24 + bytes[1] << 16 + bytes[2] << 8 + bytes[3];
	}

}
