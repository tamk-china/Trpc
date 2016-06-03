package com.tamk.Trpc.consumer;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.FactoryBean;

import com.tamk.Trpc.balence.RoundRobinBalencer;
import com.tamk.Trpc.exception.TrpcException;
import com.tamk.Trpc.pool.ConnectorPool;
import com.tamk.Trpc.protocol.InvokeTO;
import com.tamk.Trpc.route.IpCache;
import com.tamk.Trpc.route.RouteManager;
import com.tamk.Trpc.utils.ProtocolUtils;

/**
 * @author kuanqiang.tkq
 */
public class TrpcConsumer implements InvocationHandler, FactoryBean<Object> {
	private String interfaceName;

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
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

		Socket socket = ConnectorPool.INSTANCE.getSocket(RoundRobinBalencer.INSTANCE.getNextIp(interfaceName, IpCache.INSTANCE.get(interfaceName)));

		OutputStream socketOS = socket.getOutputStream();
		if (null == socketOS) {
			throw new TrpcException(String.format("outputStream of socket not exist [interfaceName = %s]", interfaceName));
		}

		byte[] body = SerializationUtils.serialize(invokeTO);
		if (0 == body.length) {
			throw new TrpcException(String.format("req body empty [interfaceName = %s]", interfaceName));
		}
		socketOS.write(ProtocolUtils.getBytes(body.length));
		socketOS.write(body);
		socketOS.flush();

		InputStream socketIS = socket.getInputStream();
		if (null == socketIS) {
			throw new TrpcException(String.format("inputStream of socket not exist [interfaceName = %s]", interfaceName));
		}

		int bodyLength = ProtocolUtils.getUnsignedInt(IOUtils.readFully(socketIS, 4));
		byte[] result = IOUtils.readFully(socketIS, bodyLength);

		return SerializationUtils.deserialize(result);
	}

	@Override
	public Object getObject() throws Exception {
		Class<?> clazz = ClassUtils.getClass(interfaceName);
		return clazz.cast(Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { clazz }, this));
	}

	@Override
	public Class<?> getObjectType() {
		try {
			return ClassUtils.getClass(interfaceName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
