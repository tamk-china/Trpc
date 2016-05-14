package com.tamk.Trpc.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.tamk.Trpc.exception.TrpcException;
import com.tamk.Trpc.protocol.InvokeTO;
import com.tamk.Trpc.route.RouteManager;

/**
 * @author kuanqiang.tkq
 */
public class TrpcProvider {
	private static Map<String, Object> invokers = new ConcurrentHashMap<String, Object>();
	private String interfaceName;
	private Object invoker;

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public void init() throws TrpcException {
		if (StringUtils.isEmpty(interfaceName)) {
			throw new TrpcException("interface can not null");
		}
		if (null == invoker) {
			throw new TrpcException("invoker can not null");
		}

		RouteManager.INSTANCE.registerProvider(interfaceName);
		invokers.put(interfaceName, invoker);
	}

	public Object invoke(InvokeTO param) throws TrpcException {
		if (null == param) {
			throw new IllegalArgumentException();
		}

		Object invoker = invokers.get(param.getInterfaceName());
		if (null == invoker) {
			throw new TrpcException(String.format("none exist for [interfaceName = %s]", param.getInterfaceName()));
		}

		try {
			Method m = invoker.getClass().getMethod(param.getFunctionName(), param.getParamClasses());
			return m.invoke(invoker, param.getParams());
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
}
