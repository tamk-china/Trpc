package com.tamk.Trpc.consumer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.tamk.Trpc.exception.TrpcException;
import com.tamk.Trpc.protocol.InvokeTO;
import com.tamk.Trpc.route.RouteManager;

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

	public void init() throws TrpcException{
		RouteManager.INSTANCE.registerConsumer(interfaceName);
	}
	
	@Override
	public Object invoke(Object arg, Method method, Object[] params) throws Throwable {
		InvokeTO invokeTO = new InvokeTO();
		invokeTO.setInterfaceName(interfaceName);
		invokeTO.setFunctionName(method.getName());
		invokeTO.setParamClasses(method.getParameterTypes());
		invokeTO.setParams(params);
		
		return null;
	}

}
